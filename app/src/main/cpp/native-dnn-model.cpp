#define _ANDROID_ true

#include <jni.h>
#include <string>
#include <pthread.h>
#include <assert.h>

#include "boost/timer.hpp"
#include "boost/progress.hpp"
#include "tiny_dnn/tiny_dnn.h"

#ifdef _ANDROID_
#include <android/log.h>
// Android log function wrappers
static const char *kTAG = "native_dnn_model";
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))
#endif

using namespace tiny_dnn;
using namespace tiny_dnn::activation;

//<editor-fold desc="formating std::string and jbyteArray">
//==================================================================================
/**
 * formating strings to jbyteArray that we can pass them to Java
 * and handle them as byte[]
 */
jbyteArray string2jbyteArray(JNIEnv * env, const std::string &nativeString) {
    jbyteArray arr = env->NewByteArray(nativeString.length());
    env->SetByteArrayRegion(arr,0,nativeString.length(),(jbyte*)nativeString.c_str());
    return arr;
}

std::string jbyteArray2string (JNIEnv * env, const jbyteArray &byteArray) {
    int len = (int) env->GetArrayLength(byteArray);
    unsigned char* buf = new unsigned char[len];
    env->GetByteArrayRegion(byteArray, 0, len, reinterpret_cast<jbyte*>(buf));
    std::string* pStr =  new std::string((char*) buf,len);
    return *pStr;
}
//==================================================================================
//</editor-fold>

//<editor-fold desc="save/load to/from BinaryString">
//==================================================================================
/**
 * This part is for saving a dnn model and its wheights to a string
 * That way we can store it in a Java string
 */
template<typename NetType>
struct _BinaryString {
    NetType t;
    std::string str;
};
template<typename NetType>
using BinaryString = struct _BinaryString<NetType>;
/**
 * save the network architecture and wheights as binary string
**/
template <typename NetType>
BinaryString<NetType> to_binary_string(network<NetType>& nn) {
    std::stringstream ss;

    cereal::BinaryOutputArchive oa(ss);
    nn.to_archive(oa, content_type::weights_and_model);

    BinaryString<NetType> bs;
    bs.str = ss.str();

    return bs;
}
/**
 * load the network architecture from binary string
**/
template <typename NetType>
void from_binary_string(const BinaryString<NetType>& bs, network<NetType>& nn) {
    std::stringstream ss;
    ss << bs.str;
    cereal::BinaryInputArchive ia(ss);
    nn.from_archive(ia, content_type::weights_and_model);
}
//==================================================================================
//</editor-fold>

// typedefs:

typedef sequential NET_TYPE;

typedef enum{
    MNIST, CIFAR10
} training_set;

typedef struct _ModelContext {
    // processing callback to java DnnModel class
	JavaVM *javaVM;
	jclass dnnModelClass;
	jobject dnnModelObject;
	pthread_mutex_t lock;
	int done;
} ModelContext;


// Global variables:
ModelContext MODEL_CONTEXT;
training_set TRAINING_SET = MNIST;
bool NN_INITIATED;
network<NET_TYPE> NN;
std::vector<vec_t> *TRAIN_DATA;
std::vector<label_t> *TRAIN_LABELS;
std::vector<vec_t> *TEST_DATA;
std::vector<label_t> *TEST_LABELS;
int NUM_OF_LABELS;
int NUM_OF_DATA;
std::string MNIST_TRAINING_DATA_FILE_NAME = "train-images.idx3-ubyte";
std::string MNIST_TRAINING_LABELS_FILE_NAME = "train-labels.idx1-ubyte";
std::string MNIST_TEST_DATA_FILE_NAME = "t10k-images.idx3-ubyte";
std::string MNIST_TEST_LABELS_FILE_NAME = "t10k-labels.idx1-ubyte";

/*
 * processing one time initialization:
 *     Cache the javaVM into our context
 *     Find class ID for JniHelper
 *     Create an instance of JniHelper
 *     Make global reference since we are using them from a native thread
 * Note:
 *     All resources allocated here are never released by application
 *     we rely on system to free all global refs when it goes away;
 *     the pairing function JNI_OnUnload() never gets called at all.
 */

extern "C"
JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	JNIEnv *env;

	memset(&MODEL_CONTEXT, 0, sizeof(MODEL_CONTEXT));

	MODEL_CONTEXT.javaVM = vm;

	//Todo: is this necessary?
	if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return JNI_ERR; // JNI version not supported.
	}
    // will be given later with a concrete object
	MODEL_CONTEXT.dnnModelClass = NULL;
	MODEL_CONTEXT.dnnModelObject = NULL;

	MODEL_CONTEXT.done = 0;

    NN_INITIATED = false;

	return JNI_VERSION_1_6;
}


// these are methods from the DnnModel java class:
extern "C"
JNIEXPORT jbyteArray JNICALL
        Java_dnnUtil_dnnModel_DnnModel_jniCreateModel(JNIEnv *env, jobject instance){
    //Todo: add content
    if(! NN_INITIATED) {
        NN << convolutional_layer<relu>(32, 32, 23, 23, 1, 1)
           << fully_connected_layer<softmax>(100, 10);
        NN_INITIATED = true;
    }
    BinaryString<NET_TYPE> binaryString = to_binary_string(NN);
    return string2jbyteArray(env, binaryString.str);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniUpdateModel(JNIEnv *env, jobject instance){
    //Todo: add content
    return NULL;//TODO: fix this
}

extern "C"
JNIEXPORT void JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniLoadModel(JNIEnv *env, jobject instance, jbyteArray binaryData){

    BinaryString<NET_TYPE> bs;
    bs.str = jbyteArray2string(env, binaryData);

    from_binary_string(bs, NN);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniTrainModel(JNIEnv *env, jobject instance){

    int minibatch_size = NUM_OF_DATA;
    int num_epochs = 1;
    int minibatchCount = 0;

    adagrad opt;
    opt.alpha *= std::sqrt(minibatch_size);

    // create callback
    auto on_enumerate_epoch = [&](){
        minibatchCount = 0;
    };

    auto on_enumerate_minibatch = [&](){
        minibatchCount++;
    };

    // training

    NN.train<mse>(opt, *TRAIN_DATA, *TRAIN_LABELS, minibatch_size, num_epochs,
                  on_enumerate_minibatch, on_enumerate_epoch);



    return NULL;
}

extern "C"
JNIEXPORT jint JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniLoadTrainingData(JNIEnv *env, jobject instance) {

    TRAIN_DATA = new std::vector<vec_t>();
    TRAIN_LABELS = new std::vector<label_t>();
    TEST_DATA = new std::vector<vec_t>();
    TEST_LABELS = new std::vector<label_t>();


    switch(TRAINING_SET){
        case MNIST:
            NUM_OF_LABELS = 10;
            parse_mnist_labels(MNIST_TRAINING_LABELS_FILE_NAME, TRAIN_LABELS);
            parse_mnist_images(MNIST_TRAINING_DATA_FILE_NAME, TRAIN_DATA, -1.0,1.0, 2, 2);
            parse_mnist_labels(MNIST_TEST_LABELS_FILE_NAME, TEST_LABELS);
            parse_mnist_images(MNIST_TEST_DATA_FILE_NAME, TEST_DATA, -1.0,1.0, 2, 2);
            break;
        case CIFAR10:
            NUM_OF_LABELS = 10;
            //parse_cifar10();
            break;
        default:
            NUM_OF_LABELS = 10;
            break;
    }
    NUM_OF_DATA = (int) TRAIN_LABELS->size();

    return (jint) NUM_OF_DATA;
}

extern "C"
JNIEXPORT void JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniGetTrainingData(JNIEnv *env, jobject instance, jint startIndex,
                                                jint endIndex) {
    jobject dnn_model_object = instance;
    jclass dnn_model_class = env->GetObjectClass(dnn_model_object);

    jmethodID initTrainingDataID = env->GetMethodID( dnn_model_class,
                                                     "initTrainingData", "(III)V");
    jmethodID setIndexTrainingDataID = env->GetMethodID( dnn_model_class,
                                                         "setIndexTrainingData", "(II[F)V");

    int numOfTrainingData = endIndex - startIndex;
    int sizeOfData = TRAIN_DATA->at(0).size();
    env->CallVoidMethod(dnn_model_object,initTrainingDataID,(jint)NUM_OF_LABELS, (jint)numOfTrainingData, (jint)sizeOfData);
    jfloatArray data_array;
    vec_t data_vec;
    label_t label;
    for( int i = startIndex; i < endIndex; i++) {
        data_vec = TRAIN_DATA->at(i);
        label = TRAIN_LABELS->at(i);

        data_array = env->NewFloatArray((jsize) data_vec.size());
        env->SetFloatArrayRegion(data_array,0,(jsize) data_vec.size(),data_vec.data());

        env->CallVoidMethod(dnn_model_object, setIndexTrainingDataID, (jint) (i-startIndex), (jint) label, data_array);

        env->DeleteLocalRef(data_array);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniSetTrainingData(JNIEnv *env, jobject instance,
                                                  jfloatArray data, jintArray labels,
                                                  jint numOfData, jint dataSize, jint numOfLabels) {
    NUM_OF_LABELS = numOfLabels;
    NUM_OF_DATA = numOfData;
    TRAIN_DATA = new std::vector<vec_t>();
    TRAIN_LABELS = new std::vector<label_t>();

    jfloat *train_data = env->GetFloatArrayElements(data, 0);
    jint *train_labels = env->GetIntArrayElements(labels, 0);

    for( int i = 0; i < NUM_OF_DATA; i++){
        TRAIN_LABELS->push_back((int) train_labels[i]);
        vec_t *vec =  new vec_t();
        for(int j = 0; j<dataSize; j++){
            vec->push_back((float) train_data[i*dataSize + j]);
        }
        TRAIN_DATA->push_back(*vec);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniGetWeightsData(JNIEnv *env, jobject instance) {
    jobject dnn_model_object = instance;
    jclass dnn_model_class = env->GetObjectClass(dnn_model_object);

    jmethodID initWeightsDataID = env->GetMethodID( dnn_model_class,
                                                     "initWeightsData", "()V");
    jmethodID setLayerWeightsID = env->GetMethodID( dnn_model_class,
                                                         "setLayerWeights", "([FI)V");
    jmethodID setLayerBiasesID = env->GetMethodID( dnn_model_class,
                                                    "setLayerBiases", "([FI)V");

    env->CallVoidMethod(dnn_model_object, initWeightsDataID);

    std::vector<vec_t*> weights;
    jfloatArray weights_array;
    jfloatArray biases_array;
    for(int i = 0; i < NN.depth(); i++){
        // 0 = weights, 1 = biases
        weights = NN[i]->weights();

        weights_array = env->NewFloatArray((jsize) weights.at(0)->size());
        biases_array = env->NewFloatArray((jsize) weights.at(1)->size());
        env->SetFloatArrayRegion(weights_array,0,(jsize) weights.at(0)->size(),weights.at(0)->data());
        env->SetFloatArrayRegion(biases_array,0,(jsize) weights.at(1)->size(),weights.at(1)->data());

        env->CallVoidMethod(dnn_model_object, setLayerWeightsID, weights_array, (jint) i);
        env->CallVoidMethod(dnn_model_object, setLayerBiasesID, biases_array, (jint) i);

        env->DeleteLocalRef(weights_array);
        env->DeleteLocalRef(biases_array);
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniSetWeightsData(JNIEnv *env, jobject instance) {
    jobject dnn_model_object = instance;
    jclass dnn_model_class = env->GetObjectClass(dnn_model_object);

    jmethodID getLayerWeightsDataID = env->GetMethodID( dnn_model_class,
                                                    "getLayerWeightsData", "(I)V");
    for(int i = 0; i < NN.depth(); i++){
        env->CallVoidMethod(dnn_model_object, getLayerWeightsDataID, (jint) i);
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniSetLayerWeightsData(JNIEnv *env, jobject instance,jint layerIndex,
                                                      jfloatArray weights, jfloatArray biases) {

    jsize weights_size = env->GetArrayLength(weights);
    jint biases_size = env->GetArrayLength(biases);

    jfloat *weights_data = env->GetFloatArrayElements(weights, 0);
    jfloat *biases_data = env->GetFloatArrayElements(biases, 0);


    std::vector<vec_t*> weights_vector = NN[layerIndex]->weights();;
    float *layer_weights = weights_vector.at(0)->data();
    float *layer_biases = weights_vector.at(1)->data();

    for( int i = 0; i < weights_size; i++){
        layer_weights[i] = weights_data[i];
    }
    for( int i = 0; i < biases_size; i++){
        layer_biases[i] = biases_data[i];
    }
}
