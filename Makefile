.PHONY: all

# JDK_PATH=/var/home/yonnji/.jdks/openjdk-23.0.1
JDK_PATH=/var/home/yonnji/.jdks/temurin-17.0.13
JAVA_SOURCE_VERSION=16
ANDROID_VERSION=24
ANDROID_SDK_PATH=/var/home/yonnji/Android/Sdk
ANDROID_KEYSTORE_PATH=/var/home/yonnji/.android/debug.keystore
ANDROID_KEYSTORE_ALIAS=android
ANDROID_STUDIO_PATH=/var/home/yonnji/Apps/Google/android-studio-2024.2.1.12-linux

ANDROID_SDK_BUILD_TOOLS_PATH=$(ANDROID_SDK_PATH)/build-tools/34.0.0
ANDROID_LIBRARY_PATH=$(ANDROID_SDK_PATH)/platforms/android-$(ANDROID_VERSION)/android.jar \

all: angelplayerwp.apk

clean:
	rm -Rf obj
	rm -Rf bin
	rm -f *.apk
	rm -f *.idsig

src/one/kitsune/angelplayerwp/R.java:
	$(ANDROID_SDK_BUILD_TOOLS_PATH)/aapt package -v -f -m \
	-M AndroidManifest.xml -I $(ANDROID_LIBRARY_PATH) -S res \
	-J $(dir $@)

classes: src/one/kitsune/angelplayerwp/R.java
	$(JDK_PATH)/bin/javac \
	--source $(JAVA_SOURCE_VERSION) \
	--target $(JAVA_SOURCE_VERSION) \
	-cp $(ANDROID_LIBRARY_PATH) \
	$(dir $<)*.java -d obj

dex: classes
	mkdir -p bin
	$(JDK_PATH)/bin/java -cp $(ANDROID_SDK_BUILD_TOOLS_PATH)/lib/d8.jar \
	com.android.tools.r8.D8 \
	--lib $(ANDROID_LIBRARY_PATH) \
	--min-api $(ANDROID_VERSION) \
	--output bin obj/one/kitsune/*/*.class

$(ANDROID_KEYSTORE_PATH):
	$(ANDROID_STUDIO_PATH)/android-studio/jbr/bin/keytool \
    -genkey -v \
    -keystore $(ANDROID_KEYSTORE_PATH) \
    -alias $(ANDROID_KEYSTORE_ALIAS) \
    -keyalg RSA \
    -keysize 2048 \
    -validity 20000 \
	  -dname "CN=kitsune.one C=JP ST=Tokyo L=Shibuya O=HQ OU=HQ"
  # set your "password"

keystore: $(ANDROID_KEYSTORE_PATH)

angelplayerwp.unsigned.unaligned.apk: classes dex
	$(ANDROID_SDK_BUILD_TOOLS_PATH)/aapt package -f \
	-M AndroidManifest.xml -I $(ANDROID_LIBRARY_PATH) -S res -f \
	--min-sdk-version $(ANDROID_VERSION) \
	--target-sdk-version $(ANDROID_VERSION) \
	-F $@ bin

angelplayerwp.unsigned.apk: angelplayerwp.unsigned.unaligned.apk
	$(ANDROID_SDK_BUILD_TOOLS_PATH)/zipalign -f -v 4 $< $@

angelplayerwp.apk: angelplayerwp.unsigned.apk $(ANDROID_KEYSTORE_PATH)
	$(JDK_PATH)/bin/java -jar $(ANDROID_SDK_BUILD_TOOLS_PATH)/lib/apksigner.jar sign \
	--ks $(ANDROID_KEYSTORE_PATH) \
	--ks-key-alias $(ANDROID_KEYSTORE_ALIAS) \
	--in $< --out $@
	$(JDK_PATH)/bin/java -jar $(ANDROID_SDK_BUILD_TOOLS_PATH)/lib/apksigner.jar verify $@

install: angelplayerwp.apk
	$(ANDROID_SDK_PATH)/platform-tools/adb install $<
