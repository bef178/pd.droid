########################################
# apk.mk
#
# must define LOCAL_* before including this file
#
# check list:
#  o. make clean && make
#  o. touch .java in src and make
#  o. touch .png/.xml in res and make
#  o. add .png/.xml in res and make
#  x. rm .java/.png/.xml and make ---- TODO
#  repeat once for .java/.png/.xml in lib folder

LIB_TYPEDEF := $(TOP)/../typedef/$(OUT_DIR)/typedef.jar

LIB_MODULE := pd-common
LIB_PACKAGE := th.pd.common.android
LIB_OUT_JAR := $(TOP)/common/$(OUT_DIR)/$(LIB_MODULE).jar
LIB_OUT_RES := $(TOP)/common/$(OUT_RES_DIR)

LOCAL_SRC_FILES := $(call find_typef, "*.java", $(LOCAL_SRC_DIR))

JAVA_R := $(OUT_SRC_DIR)/$(subst .,/,$(LOCAL_PACKAGE))/R.java
CLASSES_DEX := $(OUT_DIR)/classes.dex
OUT_APK := $(OUT_DIR)/$(LOCAL_MODULE).apk

########

$(OUT_APK): $(CLASSES_DEX) $(LOCAL_RES_DIR) $(LIB_OUT_JAR)
	@echo "Packaging ..."
	@$(AAPT) package \
		--auto-add-overlay -f \
		-M ./AndroidManifest.xml \
		-I $(ANDROID_JAR) \
		-S $(LOCAL_RES_DIR) \
		-S $(LIB_OUT_RES) \
		-F $(OUT_DIR)/$(@F).orig
	@cd $(OUT_DIR) && $(AAPT) add $(@F).orig $(notdir $(CLASSES_DEX))
	@echo "Signing ..."
	$(call sign_apk, $(OUT_DIR)/$(@F).orig, $@)

$(CLASSES_DEX): $(LOCAL_SRC_FILES) $(JAVA_R) $(LIB_OUT_JAR)
	@echo "Compiling ..."
	@-mkdir -p $(OUT_OBJ_DIR)
	@javac $(LOCAL_SRC_FILES) $(call find_typef, R.java, $(OUT_SRC_DIR)) \
		-classpath $(ANDROID_JAR):$(LIB_OUT_JAR):$(LIB_TYPEDEF) \
		-d $(OUT_OBJ_DIR)
	@$(DX) --dex \
		--output=$@ \
		$(OUT_OBJ_DIR) $(LIB_OUT_JAR)

# also generates lib's R
$(JAVA_R): $(LIB_OUT_JAR)
	@echo "Generating R ..."
	@-mkdir -p $(@D)
	@$(AAPT) package \
		--auto-add-overlay -f \
		-M ./AndroidManifest.xml \
		--extra-packages $(LIB_PACKAGE) \
		-I $(ANDROID_JAR) \
		-S $(LOCAL_RES_DIR) \
		-S $(LIB_OUT_RES) \
		-m -J $(OUT_SRC_DIR)

$(LIB_OUT_JAR):
	@echo "Checking lib ..."
	@make -C $(TOP)/common

.PHONY: clean-build
clean-build: clean $(OUT_APK)

.PHONY: install
install:
	@adb install -r $(OUT_APK)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(OUT_DIR)
