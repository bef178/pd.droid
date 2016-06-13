########
# pd-common
#
# - lib's R is used only for compiling its src
# - app generates its own R for both itself and lib
# - lib's package name is required when app generating lib's R
#

include ../build/def.mk

LOCAL_MODULE := pd-common
LOCAL_PACKAGE := th.pd.common.android
LOCAL_SRC_DIR := $(wildcard ./mod_*/src) ./src
LOCAL_RES_DIR := $(wildcard ./mod_*/res) $(wildcard ./res_mod/*)

LOCAL_SRC_FILES := $(foreach d, $(LOCAL_SRC_DIR), $(call find_typef, "*.java", $(d)))

JAVA_R := $(OUT_SRC_DIR)/$(subst .,/,$(LOCAL_PACKAGE))/R.java
OUT_JAR := $(OUT_DIR)/$(LOCAL_MODULE).jar

########

$(OUT_JAR): $(LOCAL_SRC_FILES) $(JAVA_R)
	@echo "compiling R ..."
# don't generate R.class into the package place
	@javac $(JAVA_R) \
		-d $(OUT_SRC_DIR)
	@echo "compiling java ..."
	@-mkdir -p $(OUT_OBJ_DIR)
	@javac $(LOCAL_SRC_FILES) \
		-classpath $(ANDROID_JAR):$(OUT_SRC_DIR) \
		-d $(OUT_OBJ_DIR)
	@echo "packaging ..."
# with openjdk 1.7 sometimes the R stuff appears in this directory
# have no idea why it is but just remove them for a clear package place
	@-rm -f `find $(OUT_OBJ_DIR) -regex ".*/R\($$.+\)?\.class"`
	@jar cfm $@.unsigned $(TOP)/build/manifest.mf -C $(OUT_OBJ_DIR) .
	@echo "signing ..."
	$(call sign_jar, $@.unsigned, $@)

$(JAVA_R): $(wildcard $(addsuffix /*/*,$(LOCAL_RES_DIR)))
	@echo "copying res ..."
	@-mkdir -p $(OUT_RES_DIR)
	@cp -ru $(addsuffix /*,$(LOCAL_RES_DIR)) $(OUT_RES_DIR)
	@echo "generating R ..."
	@-mkdir -p $(@D)
	@$(AAPT) package \
		--auto-add-overlay -f \
		-M ./AndroidManifest.xml \
		--non-constant-id \
		--custom-package $(LOCAL_PACKAGE) \
		-I $(ANDROID_JAR) \
		-S $(OUT_RES_DIR) \
		-m -J $(OUT_SRC_DIR)

.PHONY: clean
clean:
	@echo "cleaning ..."
	@-rm -rf $(OUT_DIR)
