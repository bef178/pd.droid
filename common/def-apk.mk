########
# def-apk.mk
#
# must define LOCAL_* before including this file
#
# check list:
#  o. make clean && make
#  o. touch .java in src and make
#  o. touch .png/.xml in res and make
#  o. add .png/.xml in res and make
#  x. rm .java/.png/.xml and make ---- TODO
#  repeat once for .java/.png/.xml in lib folder

include $(LOCAL_PD_COMMON)/def.mk
PD_COMMON_JAR := $(LOCAL_PD_COMMON)/$(PD_COMMON_JAR)
PD_COMMON_RES := $(LOCAL_PD_COMMON)/$(PD_COMMON_RES)

RES := ./res
SRC := ./src

LOCAL_GEN := ./gen
GEN_OBJ := $(LOCAL_GEN)/obj
GEN_SRC := $(LOCAL_GEN)/src

CLASSES_DEX := $(LOCAL_GEN)/classes.dex
JAVA_SRC_FILES := $(call all-typef, "*.java", $(SRC))
RES_FILES := $(wildcard $(RES)/*/*)
R_SRC_FILE := $(foreach f, $(subst .,/,$(LOCAL_PACKAGE)), $(GEN_SRC)/$(f)/R.java)

########

.PHONY: all
all: lib $(LOCAL_TARGET)

$(LOCAL_TARGET): $(LOCAL_MANIFEST) $(CLASSES_DEX) $(RES) $(PD_COMMON_RES)
	@echo "Packaging ..."
	@$(AAPT) $(AAPT_PACKAGE_FLAGS)	\
		-I $(AJAR)	\
		-S $(RES)	\
		-S $(PD_COMMON_RES)	\
		-F $(LOCAL_GEN)/t.ap1
	@cd $(LOCAL_GEN) && $(AAPT) add t.ap1 $(notdir $(CLASSES_DEX))
	@echo "Align and sign ..."
	$(call align-and-sign, $@, $(LOCAL_GEN)/t.ap1, $(LOCAL_GEN)/t.ap2)

$(CLASSES_DEX): $(JAVA_SRC_FILES) $(R_SRC_FILE) $(PD_COMMON_JAR)
	@echo "Compiling ..."
	@-mkdir -p $(GEN_OBJ)
	@javac $(JAVA_SRC_FILES) $(call all-typef, *.java, $(GEN_SRC))	\
		-classpath $(AJAR):$(PD_COMMON_JAR)	\
		-d $(GEN_OBJ)
	@$(DX) --dex		\
		--output=$@	\
		$(GEN_OBJ) $(PD_COMMON_JAR)

# also generates lib's R
$(R_SRC_FILE): $(LOCAL_MANIFEST) $(RES_FILES) $(PD_COMMON_RES)
	@echo "Generating R ..."
	@-mkdir -p $(GEN_SRC)
	@$(AAPT) $(AAPT_PACKAGE_FLAGS)	\
		--extra-packages $(PD_COMMON_PKG)	\
		-I $(AJAR)	\
		-S $(RES)	\
		-S $(PD_COMMON_RES)	\
		-m -J $(GEN_SRC)

.PHONY: install
install:
	@adb install -r $(LOCAL_TARGET)

.PHONY: lib
lib:
	@echo "Checking lib ..."
	@make -C $(LOCAL_PD_COMMON)

$(PD_COMMON_JAR):
	@make -C $(LOCAL_PD_COMMON)

$(PD_COMMON_RES): $(PD_COMMON_JAR)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@make -C $(LOCAL_PD_COMMON) clean
	@-rm -rf $(LOCAL_GEN)

