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
LOCAL_SRC_DIRS := $(wildcard ./mod_*/src) ./src
LOCAL_RES_DIRS := $(wildcard ./mod_*/res) $(wildcard ./res_mod/*)

# inferred files
SRC_FILES := $(foreach d, $(LOCAL_SRC_DIRS), $(call find_typef, "*.java", $(d)))
RES_FILES := $(wildcard $(addsuffix /*/*,$(LOCAL_RES_DIRS)))

# OUT_* are inferred variables as target
OUT_JAR := $(OUT_DIR)/$(LOCAL_MODULE).jar
OUT_JAVA_R := $(OUT_SRC_DIR)/$(subst .,/,$(LOCAL_PACKAGE))/R.java

########

$(OUT_JAR): $(SRC_FILES) $(OUT_JAVA_R)
	@echo "compiling R ..."
# don't generate R.class into the package place
	@javac $(OUT_JAVA_R) \
		-d $(OUT_SRC_DIR)
	@echo "compiling java ..."
	@-mkdir -p $(OUT_OBJ_DIR)
	@javac $(SRC_FILES) \
		-classpath $(ANDROID_JAR):$(OUT_SRC_DIR) \
		-d $(OUT_OBJ_DIR)
	@echo "packaging ..."
# with openjdk 1.7 sometimes the R stuff appears in this directory
# have no idea why it is but just remove them for a clear package place
	@-rm -f `find $(OUT_OBJ_DIR) -regex ".*/R\($$.+\)?\.class"`
	@jar cfm $@.unsigned $(TOP)/build/manifest.mf -C $(OUT_OBJ_DIR) .
	@echo "signing ..."
	$(call sign_jar, $@.unsigned, $@)

# also produce out res files
$(OUT_JAVA_R): $(RES_FILES)
	@echo "copying res ..."
	@-mkdir -p $(OUT_RES_DIR)
	@cp -ru $(addsuffix /*,$(LOCAL_RES_DIRS)) $(OUT_RES_DIR)
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
