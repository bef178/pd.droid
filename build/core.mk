########################################
# core.mk
#
# must define LOCAL_* before including this file
#
# check list:
#  o. make clean && make
#  o. touch java in src and make
#  o. touch png/xml in res and make
#  o. add png/xml in res and make
#  x. TODO rm java/png/xml and make

LOCAL_DEP_JAR += $(shell bash $(TOP)/build/get_lib_out.sh -m jar $(LOCAL_DEP_LIB))
LOCAL_DEP_PKG += $(shell bash $(TOP)/build/get_lib_out.sh -m pkg $(LOCAL_DEP_LIB))

# think of a lib depends on another lib
LOCAL_DEP_RES += $(shell bash $(TOP)/build/get_lib_out.sh -m res $(LOCAL_DEP_LIB))
LOCAL_RES_D1 := $(LOCAL_RES_D) $(LOCAL_DEP_RES)

#LOCAL_RES_F += $(foreach d,$(LOCAL_RES_D1),$(shell find -L $(d) -type f -and -not -name ".*"))
LOCAL_RES_F += $(foreach d,$(LOCAL_RES_D1),$(wildcard $(addsuffix /*/*,$(d))))

LOCAL_SRC_F += $(foreach d,$(LOCAL_SRC_D),$(shell find -L $(d) -type f -name "*.java" -and -not -name ".*"))

$(call assign_if_not_yet,LOCAL_PKG_S,$(shell bash $(TOP)/build/get_lib_out.sh -m pkg $(LOCAL_AMF_F)))

########

# *OUT_* are inferred variables as target
OUT_RES_D := $(LOCAL_OUT_D)/res
OUT_SRC_D := $(LOCAL_OUT_D)/src
OUT_OBJ_D := $(LOCAL_OUT_D)/obj
OUT_AMF_F := $(LOCAL_OUT_D)/AndroidManifest.xml
OUT_R_F   := $(OUT_SRC_D)/$(subst .,/,$(LOCAL_PKG_S))/R.java
OUT_JAR   := $(LOCAL_OUT_D)/$(LOCAL_PKG_S).jar
OUT_DEX_F := $(OUT_OBJ_D)/classes.dex
OUT_APK   := $(LOCAL_OUT_D)/$(LOCAL_PKG_S).apk

########

ifeq ($(LOCAL_IS_LIB),true)

.PHONY: all
all: lib

.PHONY: lib
lib: $(OUT_JAR)
	@make $(MAKECMDGOALS) -C demo

$(OUT_JAR): $(LOCAL_RES_D) $(OUT_R_F) $(LOCAL_SRC_F)
	@echo "Compiling R ..."
# don't generate R.class into the package place
	@javac $(OUT_R_F) \
		-d $(OUT_SRC_D)
	@echo "Compiling java ..."
	@-mkdir -p $(OUT_OBJ_D)
	@javac $(LOCAL_SRC_F) \
		-classpath $(ANDROID_JAR):$(OUT_SRC_D) \
		-d $(OUT_OBJ_D)
	@echo "Packaging ..."
# with openjdk 1.7 sometimes the R stuff appears in this directory
# have no idea why it is but just remove them for a clear package place
	@-rm -f `find $(OUT_OBJ_D) -regex ".*/R\($$.+\)?\.class"`
	@jar cfm $@.unsigned ../build/manifest.mf -C $(OUT_OBJ_D) .
	@echo "Signing ..."
	$(call sign_jar,$@.unsigned,$@,$(LOCAL_SIGN_WITHOUT_TSA)) >/dev/null

else

.PHONY: all
all: apk

.PHONY: apk
apk: $(OUT_APK)

$(OUT_APK): $(OUT_AMF_F) $(OUT_DEX_F) $(LOCAL_RES_D1)
	@echo "Packaging ..."
	@$(AAPT) package \
		--auto-add-overlay -f \
		-M $(OUT_AMF_F) \
		-I $(ANDROID_JAR) \
		$(addprefix -S ,$(LOCAL_RES_D1)) \
		-F $(LOCAL_OUT_D)/$(@F).unsigned
	@$(AAPT) add -k $(@).unsigned $(OUT_DEX_F) >/dev/null
	@echo "Signing ..."
	$(call sign_apk,$(LOCAL_OUT_D)/$(@F).unsigned,$@,$(LOCAL_SIGN_WITHOUT_TSA)) >/dev/null

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

.PHONY: install
install:
	@adb install -r $(OUT_APK)

endif

# copy manifest so it's easier to change package/version
$(OUT_AMF_F): $(LOCAL_AMF_F)
	@echo "Generating manifest ..."
	@-mkdir -p $(@D)
	@cp $(LOCAL_AMF_F) $@

$(OUT_R_F): $(LOCAL_RES_D1) $(LOCAL_RES_F)
$(OUT_R_F): $(LOCAL_DEP_JAR) $(OUT_AMF_F)
ifeq ($(LOCAL_IS_LIB),true)
	@echo "Copying res ..."
	@-mkdir -p $(OUT_RES_D)
	@cp -ru $(addsuffix /*,$(LOCAL_RES_D)) $(OUT_RES_D)
endif
	@echo "Generating R ..."
	@-mkdir -p $(@D)
	@$(AAPT) package \
		--auto-add-overlay -f \
		$(addprefix --extra-packages ,$(LOCAL_DEP_PKG)) \
		-M $(OUT_AMF_F) \
		$(shell if test "$(LOCAL_IS_LIB)" = "true"; then echo --non-constant-id; fi) \
		--custom-package $(LOCAL_PKG_S) \
		-I $(ANDROID_JAR) \
		$(addprefix -S ,$(LOCAL_RES_D1)) \
		-m -J $(OUT_SRC_D)

.PHONY: clean-build
clean-build: clean all

.PHONY: clean
clean:
	@echo "Cleaning ..."
	@-rm -rf $(LOCAL_OUT_D)
