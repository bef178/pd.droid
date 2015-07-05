########
# pd-mail

PD_COMMON := ../common
include $(PD_COMMON)/def.mk
PD_COMMON_JAR := $(PD_COMMON)/$(PD_COMMON_JAR)
PD_COMMON_RES := $(PD_COMMON)/$(PD_COMMON_RES)

RES := ./res
SRC := ./src

GEN := ./gen
GEN_OBJ := $(GEN)/obj
GEN_SRC := $(GEN)/src

PKG := th.pd.mail
TARGET := $(GEN)/pd-mail.apk

CLASSES_DEX := $(GEN)/classes.dex
JAVA_SRC_FILES := $(call all-typef, "*.java", $(SRC))
R_SRC_FILE := $(foreach f, $(subst .,/,$(PKG)), $(GEN_SRC)/$(f)/R.java)

########

$(TARGET): $(CLASSES_DEX) $(RES) $(PD_COMMON_RES)
	@echo "Packaging ..."
	@$(AAPT) $(AAPT_PACKAGE_FLAGS)	\
		-I $(AJAR)	\
		-S $(RES)	\
		-S $(PD_COMMON_RES)	\
		-F $(GEN)/t.ap1
	@cd $(GEN) && $(AAPT) add t.ap1 $(notdir $(CLASSES_DEX))
	@echo "Align and sign ..."
	$(call align-and-sign, $@, $(GEN)/t.ap1, $(GEN)/t.ap2)

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
$(R_SRC_FILE): $(RES) $(PD_COMMON_RES)
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
	@adb install -r $(TARGET)

.PHONY: promise
promise: $(PD_COMMON_JAR)

$(PD_COMMON_JAR):
	@make -C $(PD_COMMON)

$(PD_COMMON_RES): $(PD_COMMON_JAR)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@make -C $(PD_COMMON) clean
	@-rm -rf $(GEN)

