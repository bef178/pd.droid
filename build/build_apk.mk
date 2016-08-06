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

LOCAL_DEP_PKG_S ?=
LOCAL_DEP_JAR_F ?=
LOCAL_DEP_RES_D ?=

$(call assign_if_not_yet,LOCAL_RES_D,./res)

$(call assign_if_not_yet,LOCAL_SRC_D,./src)
LOCAL_SRC_F := $(shell find -L $(LOCAL_SRC_D) -type f -name "*.java" -and -not -name ".*")

$(call assign_if_not_yet,LOCAL_AMF_F,./AndroidManifest.xml)
$(call assign_if_not_yet,LOCAL_PKG_S,$(shell grep -oe 'package="\(.*\)"' $(LOCAL_AMF_F) | cut -d \" -f2))

$(call assign_if_not_yet,LOCAL_OUT_D,./out)

LOCAL_SIGN_WITH_TSA ?= true

########

OUT_RES_D := $(LOCAL_OUT_D)/res
OUT_SRC_D := $(LOCAL_OUT_D)/src
OUT_OBJ_D := $(LOCAL_OUT_D)/obj
OUT_AMF_F := $(LOCAL_OUT_D)/AndroidManifest.xml

OUT_SRC_F := $(subst $(LOCAL_SRC_D),$(OUT_SRC_D),$(LOCAL_SRC_F))
OUT_R_F   := $(OUT_SRC_D)/$(subst .,/,$(LOCAL_PKG_S))/R.java
OUT_DEX_F := $(OUT_OBJ_D)/classes.dex
OUT_APK   := $(LOCAL_OUT_D)/$(LOCAL_PKG_S).apk

########

$(OUT_APK): $(OUT_AMF_F) $(OUT_DEX_F) $(LOCAL_RES_D) $(LOCAL_DEP_RES_D)
	@echo "Packaging ..."
	@$(AAPT) package \
		--auto-add-overlay -f \
		-M $(OUT_AMF_F) \
		-I $(ANDROID_JAR) \
		-S $(LOCAL_RES_D) \
		-S $(LOCAL_DEP_RES_D) \
		-F $(LOCAL_OUT_D)/$(@F).orig
	@$(AAPT) add -k $(@).orig $(OUT_DEX_F) >/dev/null
	@echo "Signing ..."
	$(call sign_jar,$(LOCAL_OUT_D)/$(@F).orig,$@,$(LOCAL_SIGN_WITH_TSA)) >/dev/null

# copy manifest so it's easier to change package/version
$(OUT_AMF_F): $(LOCAL_AMF_F)
	@echo "Generating manifest ..."
	@-mkdir -p $(@D)
	@cp $(LOCAL_AMF_F) $@

ifneq ($(LOCAL_DEP_RES_D),)
$(OUT_R_F): $(LOCAL_DEP_RES_D) $(shell find -L $(LOCAL_DEP_RES_D) -type f -and -not -name ".*")
endif
$(OUT_R_F): $(LOCAL_RES_D) $(shell find -L $(LOCAL_RES_D) -type f -and -not -name ".*")
$(OUT_R_F): $(LOCAL_DEP_JAR_F) $(OUT_AMF_F)
	@echo "Generating R ..."
	@-mkdir -p $(@D)
	@$(AAPT) package \
		--auto-add-overlay -f \
		$(shell if test -n "$(LOCAL_DEP_PKG_S)"; then echo --extra-packages $(LOCAL_DEP_PKG_S); fi) \
		-M $(OUT_AMF_F) \
		-I $(ANDROID_JAR) \
		-S $(LOCAL_RES_D) \
		$(shell if test -n "$(LOCAL_DEP_RES_D)"; then echo -S $(LOCAL_DEP_RES_D); fi) \
		-m -J $(OUT_SRC_D)

ifneq ($(LOCAL_DEP_JAR_F),)
$(OUT_DEX_F): $(LOCAL_DEP_JAR_F)
endif
$(OUT_DEX_F): $(LOCAL_SRC_F) $(OUT_R_F)
	@echo "Compiling ..."
	@-mkdir -p $(OUT_OBJ_D)
	@javac $(LOCAL_SRC_F) $(shell find -L $(OUT_SRC_D) -type f -name R.java) \
		-classpath $(ANDROID_JAR)$(shell if test -n "$(LOCAL_DEP_JAR_F)"; then echo " "$(LOCAL_DEP_JAR_F) | sed "s/ \\+/:/g"; fi) \
		-d $(OUT_OBJ_D)
	@$(DX) --dex \
		--output=$@ \
		$(OUT_OBJ_D) $(LOCAL_DEP_JAR_F)

.PHONY: clean-build
clean-build: clean $(OUT_APK)

.PHONY: install
install:
	@adb install -r $(OUT_APK)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(LOCAL_OUT_D)
