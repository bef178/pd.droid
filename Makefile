#

PD_COMMON_DIR := ./common
include $(PD_COMMON_DIR)/def.mk
PD_COMMON_JAR := $(PD_COMMON_DIR)/$(PD_COMMON_JAR)
PD_COMMON_RES := $(PD_COMMON_DIR)/$(PD_COMMON_RES)

RES := ./res
SRC := ./src
GEN := ./gen

GEN_OBJ := $(GEN)/obj
GEN_SRC := $(GEN)/src

TARGET := pd.apk

.PHONY: all
all: $(GEN)/$(TARGET)

$(PD_COMMON_JAR) $(PD_COMMON_RES):
	@make -C $(PD_COMMON_DIR)

.PHONY: R
R: $(RES) $(PD_COMMON_RES)
	@-mkdir -p $(GEN_SRC)
	@echo "Generating R ..."
	@$(AAPT) $(AAPT_PACKAGE_FLAGS)	\
		--extra-packages $(PD_COMMON_PKG)	\
		-I $(AJAR)	\
		-S $(RES)	\
		-S $(PD_COMMON_RES)	\
		-m -J $(GEN_SRC)

DEX := classes.dex
$(GEN)/$(DEX): R $(PD_COMMON_JAR)
	@echo "Compiling ..."
	@-mkdir -p $(GEN_OBJ)
	@javac $(call all-typef, *.java, $(GEN_SRC)) $(call all-typef, *.java, $(SRC))	\
		-sourcepath $(SRC):$(GEN_SRC)	\
		-classpath $(AJAR):$(PD_COMMON_JAR)	\
		-d $(GEN_OBJ)
	@$(DX) --dex --output=$@ $(GEN_OBJ) $(PD_COMMON_JAR)

$(GEN)/$(TARGET): $(GEN)/$(DEX) $(PD_COMMON_RES)
	@echo "Packaging ..."
	@-mkdir -p $(GEN)
	@$(AAPT) $(AAPT_PACKAGE_FLAGS)	\
		-I $(AJAR)	\
		-S $(RES)	\
		-S $(PD_COMMON_RES)	\
		-F $(GEN)/t.ap1
	@cd $(GEN) && $(AAPT) add t.ap1 $(DEX)
	@echo "Aligning ..."
	@$(ZIPALIGN) -f 4 $(GEN)/t.ap1 $(GEN)/t.ap2
	@echo "Signing ..."
	@jarsigner	\
		-tsa http://timestamp.digicert.com	\
		-keystore $(KEYSTORE)	\
		-storepass android	\
		-signedjar $@	\
		$(GEN)/t.ap2 $(CERT)

.PHONY: install
install:
	@adb install -r $(GEN)/$(TARGET)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN)
	@make -C $(PD_COMMON_DIR) clean

