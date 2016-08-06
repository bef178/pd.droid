########
# pd-common
#
# - lib's R is used only for compiling its src
# - app generates its own R for both itself and lib
# - lib's package name is required when app generating lib's R
#

TOP := ..
-include $(TOP)/build/env.mk
include $(TOP)/build/f.mk
include include.mk

LOCAL_RES_D ?= $(wildcard ./mod_*/res) $(wildcard ./res_mod/*)
LOCAL_RES_F := $(wildcard $(addsuffix /*/*,$(LOCAL_RES_D)))

LOCAL_SRC_D ?= $(wildcard ./mod_*/src) ./src
LOCAL_SRC_F := $(foreach d, $(LOCAL_SRC_D), $(call find_typef, "*.java", $(d)))

LOCAL_AMF_F ?= ./AndroidManifest.xml
LOCAL_PKG_S ?= $(shell grep -oe 'package="\(.*\)"' $(LOCAL_AMF_F) | cut -d \" -f2)

LOCAL_OUT_D ?= ./out

LOCAL_SIGN_WITH_TSA ?= false

########

# *OUT_* are inferred variables as target
OUT_RES_D := $(LOCAL_OUT_D)/res
OUT_SRC_D := $(LOCAL_OUT_D)/src
OUT_OBJ_D := $(LOCAL_OUT_D)/obj
OUT_AMF_F := $(LOCAL_OUT_D)/AndroidManifest.xml
OUT_R_F := $(OUT_SRC_D)/$(subst .,/,$(LOCAL_PKG_S))/R.java
OUT_JAR := $(LOCAL_OUT_D)/$(LOCAL_PKG_S).jar

########

.PHONY: demo
demo: $(OUT_JAR)
	@make -ef demo.mk

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
	$(call sign_jar,$@.unsigned,$@,$(LOCAL_SIGN_WITH_TSA)) >/dev/null

# copy manifest so it's easier to change package/version
$(OUT_AMF_F): $(LOCAL_AMF_F)
	@echo "Generating manifest ..."
	@-mkdir -p $(@D)
	@cp $(LOCAL_AMF_F) $@

$(OUT_R_F): $(OUT_AMF_F) $(LOCAL_RES_F)
	@echo "Copying res ..."
	@-mkdir -p $(OUT_RES_D)
	@cp -ru $(addsuffix /*,$(LOCAL_RES_D)) $(OUT_RES_D)
	@echo "Generating R ..."
	echo $(@D)
	@-mkdir -p $(@D)
	@$(AAPT) package \
		--auto-add-overlay -f \
		-M $(OUT_AMF_F) \
		--non-constant-id \
		--custom-package $(LOCAL_PKG_S) \
		-I $(ANDROID_JAR) \
		-S $(OUT_RES_D) \
		-m -J $(OUT_SRC_D)

.PHONY: clean
clean:
	@echo "cleaning ..."
	@-rm -rf $(LOCAL_OUT_D)
