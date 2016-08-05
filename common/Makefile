########
# pd-common
#
# - lib's R is used only for compiling its src
# - app generates its own R for both itself and lib
# - lib's package name is required when app generating lib's R
#

include ../build/def.mk
include include.mk

SRC_DIR := $(wildcard ./mod_*/src) ./src
RES_DIR := $(wildcard ./mod_*/res) $(wildcard ./res_mod/*)

# inferred files
SRC_FILES := $(foreach d, $(SRC_DIR), $(call find_typef, "*.java", $(d)))
RES_FILES := $(wildcard $(addsuffix /*/*,$(RES_DIR)))

# *OUT_* are inferred variables as target
OUT_JAVA_R := $(OUT_SRC_DIR)/$(subst .,/,$(LOCAL_PACKAGE))/R.java

########

.PHONY: demo
demo: $(LOCAL_OUT_JAR)
	@make -f demo.mk

$(LOCAL_OUT_JAR): $(SRC_FILES) $(OUT_JAVA_R)
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
	$(call sign_jar, $@.unsigned, $@) >/dev/null

# also produce out res files
$(OUT_JAVA_R): $(RES_FILES)
	@echo "copying res ..."
	@-mkdir -p $(OUT_RES_DIR)
	@cp -ru $(addsuffix /*,$(RES_DIR)) $(OUT_RES_DIR)
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
