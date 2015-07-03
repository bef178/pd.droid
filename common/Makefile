########
# - lib's R is used only for compiling its src
# - apk generates its own R for both itself and lib
# - lib's package name is required when apk generating lib's R
# so provide package name, jar(classes) and res

include ./def.mk

GEN := ./gen
GEN_OBJ := $(GEN)/obj
GEN_RES := $(GEN)/res
GEN_R := $(GEN)/R
RES := ./res_mod
SRC := ./src

GEN_DIRS := $(GEN) $(GEN_OBJ) $(GEN_RES) $(GEN_R)

PKG := th.pd.common.android

JAVA_SRC_FILES := $(call all-typef, "*.java", $(SRC))
JAVA_OBJ_FILES := $(patsubst $(SRC)/%.java, $(GEN_OBJ)/%.class, $(JAVA_SRC_FILES))
R_SRC_FILE := $(GEN_R)/$(subst .,/,$(PKG))/R.java
R_OBJ_FILE := $(patsubst %.java, %.class, $(R_SRC_FILE))

RES_SRC_FILES := $(wildcard $(RES)/*/*/*)
RES_TARGET_FILES := $(foreach m,	\
		$(wildcard $(RES)/*),	\
		$(subst $m,		\
			$(GEN_RES),	\
			$(wildcard $m/*/*)))

TARGET := $(GEN)/pd-common.jar

########

$(TARGET): $(GEN_DIRS) $(JAVA_OBJ_FILES)
	@echo "Packaging ..."
# as lib, remove the R stuff
	@jar cf $@ -C $(GEN_OBJ) .

$(JAVA_OBJ_FILES): $(JAVA_SRC_FILES) $(R_OBJ_FILE)
	@echo "Compiling ..."
	@javac $(JAVA_SRC_FILES)	\
		-classpath $(AJAR):$(GEN_R)	\
		-d $(GEN_OBJ)

$(R_OBJ_FILE): $(RES_TARGET_FILES)
	@echo "Generating R ..."
	@$(AAPT) $(AAPT_PACKAGE_FLAGS)	\
		--non-constant-id	\
		--custom-package $(PKG)	\
		-I $(AJAR)	\
		-S $(GEN_RES)	\
		-m -J $(GEN_R)
	@javac $(R_SRC_FILE)

$(RES_TARGET_FILES): $(RES_SRC_FILES)
	@echo "Generating res ..."
	@cp -r $(RES)/*/* $(GEN_RES)

$(GEN_DIRS):
	@-mkdir -p $(GEN_DIRS)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN)
