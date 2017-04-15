#include <jni.h>
#include <string>
#include <pthread.h>
#include <jni.h>
#include <android/log.h>
#include <assert.h>

#include <boost/timer.hpp>
#include <boost/progress.hpp>

#include "tiny_dnn/tiny_dnn.h"

using namespace tiny_dnn;
using namespace tiny_dnn::activation;

// Android log function wrappers
static const char *kTAG = "native_dnn_model";
#define LOGI(...) \
  ((void)__android_log_print(ANDROID_LOG_INFO, kTAG, __VA_ARGS__))
#define LOGW(...) \
  ((void)__android_log_print(ANDROID_LOG_WARN, kTAG, __VA_ARGS__))
#define LOGE(...) \
  ((void)__android_log_print(ANDROID_LOG_ERROR, kTAG, __VA_ARGS__))

//<editor-fold desc="formatin std::string and jbyteArray">
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
    {
        cereal::BinaryOutputArchive oa(ss);
        nn.to_archive(oa, content_type::weights_and_model);
    }

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
    nn.from_archive(ia, content_type::model);
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
network<NET_TYPE> NN;
std::vector<vec_t> *TRAIN_DATA;
std::vector<label_t> *TRAIN_LABELS;
std::vector<vec_t> *TEST_DATA;
std::vector<label_t> *TEST_LABELS;
std::string MNIST_TRAINING_DATA_FILE_NAME = "";
std::string MNIST_TRAINING_LABELS_FILE_NAME = "";
std::string MNIST_TEST_DATA_FILE_NAME = "";
std::string MNIST_TEST_LABELS_FILE_NAME = "";

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

    //Todo: maybe this is not needed
    memset(&NN, 0, sizeof(network<NET_TYPE>));

	MODEL_CONTEXT.javaVM = vm;

	//Todo: is this necessary?
	if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) != JNI_OK) {
		return JNI_ERR; // JNI version not supported.
	}
    // will be given later with a concrete object
	MODEL_CONTEXT.dnnModelClass = NULL;
	MODEL_CONTEXT.dnnModelObject = NULL;

	MODEL_CONTEXT.done = 0;

	return JNI_VERSION_1_6;
}


// these are methods from the DnnModel java class:
extern "C"
JNIEXPORT jbyteArray JNICALL
        Java_dnnUtil_dnnModel_DnnModel_jniCreateModel(JNIEnv *env, jobject instance){
    //Todo: add content
    NN << convolutional_layer<relu>(32,32,23,23,1,100)
       << fully_connected_layer<softmax>(100,10);

    BinaryString<NET_TYPE> binaryString = to_binary_string(NN);
    return string2jbyteArray(env, binaryString.str);
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniUpdateModel(JNIEnv *env, jobject instance){
    //Todo: add content
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
    //Todo: add content
}

JNIEXPORT void JNICALL
Java_dnnUtil_dnnModel_DnnModel_jniLoadTrainingData(JNIEnv *env, jobject instance) {
    TRAIN_DATA = new std::vector<vec_t>();
    TRAIN_LABELS = new std::vector<label_t>();
    TEST_DATA = new std::vector<vec_t>();
    TEST_LABELS = new std::vector<label_t>();

    switch(TRAINING_SET){
        case MNIST:
            parse_mnist_labels(MNIST_TRAINING_LABELS_FILE_NAME, TRAIN_LABELS);
            parse_mnist_images(MNIST_TRAINING_DATA_FILE_NAME, TRAIN_DATA, -1.0,1.0, 2, 2);
            parse_mnist_labels(MNIST_TEST_LABELS_FILE_NAME, TRAIN_LABELS);
            parse_mnist_images(MNIST_TEST_DATA_FILE_NAME, TRAIN_DATA, -1.0,1.0, 2, 2);
            break;
        case CIFAR10:
            //parse_cifar10();
            break;
        default:
            break;
    }


}