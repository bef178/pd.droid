########################################
# build_apk.mk
#
# must define LOCAL_* before including this file
#
# check list:
#  o. make clean && make
#  o. touch java in src and make
#  o. touch png/xml in res and make
#  o. add png/xml in res and make
#  x. TODO rm java/png/xml and make

LOCAL_DEP_JAR += $(shell bash $(TOP)/build/get_lib_out -m jar $(LOCAL_DEP_LIB))
LOCAL_DEP_PKG += $(shell bash $(TOP)/build/get_lib_out -m pkg $(LOCAL_DEP_LIB))

LOCAL_RES_D += $(shell bash $(TOP)/build/get_lib_out -m res $(LOCAL_DEP_LIB))
LOCAL_RES_F += $(foreach d,$(LOCAL_RES_D),$(shell find -L $(d) -type f -and -not -name ".*"))

LOCAL_SRC_F += $(foreach d,$(LOCAL_SRC_D),$(shell find -L $(d) -type f -name "*.java" -and -not -name ".*"))

$(call assign_if_not_yet,LOCAL_PKG_S,$(shell bash $(TOP)/build/get_lib_out -m pkg $(LOCAL_AMF_F)))

########

OUT_SRC_D := $(LOCAL_OUT_D)/src
OUT_OBJ_D := $(LOCAL_OUT_D)/obj
OUT_AMF_F := $(LOCAL_OUT_D)/AndroidManifest.xml

OUT_R_F   := $(OUT_SRC_D)/$(subst .,/,$(LOCAL_PKG_S))/R.java
OUT_DEX_F := $(OUT_OBJ_D)/classes.dex
OUT_APK   := $(LOCAL_OUT_D)/$(LOCAL_PKG_S).apk

########

$(OUT_APK): $(OUT_AMF_F) $(OUT_DEX_F) $(LOCAL_RES_D)
	@echo "Packaging ..."
	@$(AAPT) package \
		--auto-add-overlay -f \
		-M $(OUT_AMF_F) \
		-I $(ANDROID_JAR) \
		$(addprefix -S ,$(LOCAL_RES_D)) \
		-F $(LOCAL_OUT_D)/$(@F).unsigned
	@$(AAPT) add -k $(@).unsigned $(OUT_DEX_F) >/dev/null
	@echo "Signing ..."
	$(call sign_jar,$(LOCAL_OUT_D)/$(@F).unsigned,$@,$(LOCAL_SIGN_WITH_TSA)) >/dev/null

# copy manifest so it's easier to change package/version
$(OUT_AMF_F): $(LOCAL_AMF_F)
	@echo "Generating manifest ..."
	@-mkdir -p $(@D)
	@cp $(LOCAL_AMF_F) $@

$(OUT_R_F): $(LOCAL_RES_D) $(LOCAL_RES_F)
$(OUT_R_F): $(LOCAL_DEP_JAR) $(OUT_AMF_F)
	@echo "Generating R ..."
	@-mkdir -p $(@D)
	@$(AAPT) package \
		--auto-add-overlay -f \
		$(addprefix --extra-packages ,$(LOCAL_DEP_PKG)) \
		-M $(OUT_AMF_F) \
		-I $(ANDROID_JAR) \
		$(addprefix -S ,$(LOCAL_RES_D)) \
		-m -J $(OUT_SRC_D)

$(OUT_DEX_F): $(LOCAL_DEP_JAR)
$(OUT_DEX_F): $(LOCAL_SRC_F) $(OUT_R_F)
	@echo "Compiling ..."
	@-mkdir -p $(OUT_OBJ_D)
	@javac $(LOCAL_SRC_F) $(shell find -L $(OUT_SRC_D) -type f -name R.java) \
		-classpath $(ANDROID_JAR)$(shell if test -n "$(LOCAL_DEP_JAR)"; then echo " "$(LOCAL_DEP_JAR) | sed "s/ \\+/:/g"; fi) \
		-d $(OUT_OBJ_D)
	@$(DX) --dex \
		--output=$@ \
		$(OUT_OBJ_D) $(LOCAL_DEP_JAR)

.PHONY: clean-build
clean-build: clean $(OUT_APK)

.PHONY: install
install:
	@adb install -r $(OUT_APK)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(LOCAL_OUT_D)
