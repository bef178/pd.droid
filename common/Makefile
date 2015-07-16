########
# pd-common
#
# - lib's R is used only for compiling its src
# - apk generates its own R for both itself and lib
# - lib's package name is required when apk generating lib's R
# so provide package name, jar(classes) and res

include ./def.mk

RES := ./res_mod
SRC := ./src

GEN := ./gen
GEN_OBJ := $(GEN)/obj
GEN_R := $(GEN)/R

PKG := th.pd.common.android
TARGET_JAR := $(GEN)/pd-common.jar
TARGET_RES := $(GEN)/res

TARGET_RES_FILES := $(foreach		\
		m,			\
		$(wildcard $(RES)/*),	\
		$(subst $m,		\
			$(TARGET_RES),	\
			$(wildcard $m/*/*)))
JAVA_SRC_FILES := $(call all-typef, "*.java", $(SRC))
R_SRC_FILE := $(GEN_R)/$(subst .,/,$(PKG))/R.java
R_OBJ_FILE := $(patsubst %.java, %.class, $(R_SRC_FILE))

########

$(TARGET_JAR): $(JAVA_SRC_FILES) $(R_OBJ_FILE)
	@echo "Compiling ..."
	@-mkdir -p $(GEN_OBJ)
	@javac $(JAVA_SRC_FILES)	\
		-classpath $(AJAR):$(GEN_R)	\
		-d $(GEN_OBJ)
	@echo "Packaging ..."
# with openjdk 1.7 sometimes the R stuff appears in this directory
# have no idea why it is but just remove them for a clear package place
	@-rm -f `find $(GEN_OBJ) -regex ".*/R\($$.+\)?\.class"`
	@jar cf $@ -C $(GEN_OBJ) .

$(R_OBJ_FILE): $(TARGET_RES_FILES)
	@echo "Generating R ..."
	@-mkdir -p $(GEN_R)
	@$(AAPT) $(AAPT_PACKAGE_FLAGS)	\
		--non-constant-id	\
		--custom-package $(PKG)	\
		-I $(AJAR)	\
		-S $(TARGET_RES)	\
		-m -J $(GEN_R)
# don't generate R.class into the package place
	@javac $(R_SRC_FILE)	\
		-d $(GEN_R)

# generate each taregt res file
$(TARGET_RES)/%: $(RES)/*/%
	@-mkdir -p $(dir $@)
	@-cp -ru $< $@

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN)
