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

define find_type_f
	$(shell find -L $(2) -type f -name $(strip $(1)) -and -not -name ".*")
endef

define do_assign_if_not_yet
	ifndef $(1)
		$(1) := $(2)
	endif
	ifeq ($($(1)),)
		$(1) := $(2)
	endif
endef
define assign_if_not_yet
	$(eval $(call do_$(0),$(1),$(2)))
endef

####

ANDROID_JAR := $(ANDROID_HOME)/platforms/$(LOCAL_API_REV)/android.jar
$(info using $(ANDROID_JAR))

LOCAL_BUILDER_REV ?= $(shell \ls $(ANDROID_HOME)/build-tools | sort -nr | head -1)
BUILDER := $(ANDROID_HOME)/build-tools/$(LOCAL_BUILDER_REV)
$(info using $(BUILDER))
PATH := $(PATH):$(BUILDER)

####

LOCAL_DEP_JAR += $(shell bash $(TOP)/build/get_metadata.sh -m jar $(LOCAL_DEP_LIB))
LOCAL_DEP_PKG += $(shell bash $(TOP)/build/get_metadata.sh -m pkg $(LOCAL_DEP_LIB))

# think of a lib depends on another lib
LOCAL_DEP_RES += $(shell bash $(TOP)/build/get_metadata.sh -m res $(LOCAL_DEP_LIB))
LOCAL_RES_D1 := $(LOCAL_RES_D) $(LOCAL_DEP_RES)

#LOCAL_RES_F += $(foreach d,$(LOCAL_RES_D1),$(shell find -L $(d) -type f -and -not -name ".*"))
LOCAL_RES_F += $(foreach d,$(LOCAL_RES_D1),$(wildcard $(addsuffix /*/*,$(d))))

LOCAL_SRC_F += $(foreach d,$(LOCAL_SRC_D),$(shell find -L $(d) -type f -name "*.java" -and -not -name ".*"))

$(call assign_if_not_yet,LOCAL_PKG_S,$(shell bash $(TOP)/build/get_metadata.sh -m pkg $(LOCAL_AMF_F)))

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
		-classpath $(ANDROID_JAR):$(OUT_SRC_D)$(shell if test -n "$(LOCAL_DEP_JAR)"; then echo " "$(LOCAL_DEP_JAR) | sed "s/ \\+/:/g"; fi) \
		-d $(OUT_OBJ_D)
	@echo "Packaging ..."
# with openjdk 1.7 sometimes the R stuff appears in this directory
# have no idea why it is but just remove them for a clear package place
	@-rm -f `find $(OUT_OBJ_D) -regex ".*/R\($$.+\)?\.class"`
	@jar cfm $@ ../build/manifest.mf -C $(OUT_OBJ_D) .

else

.PHONY: all
all: apk

.PHONY: apk
apk: $(OUT_APK)

$(OUT_APK): $(OUT_AMF_F) $(OUT_DEX_F) $(LOCAL_RES_D1)
	@echo "Packaging ..."
	@ PATH=$(PATH) aapt package \
		--auto-add-overlay -f \
		-M $(OUT_AMF_F) \
		-I $(ANDROID_JAR) \
		$(addprefix -S ,$(LOCAL_RES_D1)) \
		-F $@.unsigned
	@ PATH=$(PATH) aapt add -k $@.unsigned $(OUT_DEX_F) >/dev/null
	@echo "Signing ..."
	@ PATH=$(PATH) bash $(TOP)/build/sign_apk.sh \
		--store_name $(KEYSTORE) \
		--store_pass $(KEYSTORE_PASS) \
		--key_name $(KEYSTORE_KEY_NAME) \
		$@.unsigned $@

$(OUT_DEX_F): $(LOCAL_DEP_JAR)
$(OUT_DEX_F): $(LOCAL_SRC_F) $(OUT_R_F)
	@echo "Compiling ..."
	@-mkdir -p $(OUT_OBJ_D)
	@javac $(LOCAL_SRC_F) $(shell find -L $(OUT_SRC_D) -type f -name R.java) \
		-classpath $(ANDROID_JAR)$(shell if test -n "$(LOCAL_DEP_JAR)"; then echo " "$(LOCAL_DEP_JAR) | sed "s/ \\+/:/g"; fi) \
		-d $(OUT_OBJ_D)
	@ PATH=$(PATH) dx --dex \
		--output=$@ \
		$(OUT_OBJ_D) $(LOCAL_DEP_JAR)

.PHONY: install
install: apk
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
	@ PATH=$(PATH) aapt package \
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
