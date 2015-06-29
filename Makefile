#

define all-typef
	`find -L $(2) -type f -name "$(strip $(1))"`
endef

ADT_HOME := $(HOME)/app/android-sdk-linux
ADT_BUILD := $(ADT_HOME)/build-tools/22.0.1
AAPT := $(ADT_BUILD)/aapt
AAPT_FLAGS := --auto-add-overlay -f -M ./AndroidManifest.xml
DX := $(ADT_BUILD)/dx
AJAR := $(ADT_HOME)/platforms/android-19/android.jar
KEYSTORE := $(HOME)/conf/th.keystore
CERT := cert

RES := ./res
SRC := ./src
GEN := ./gen

GEN_OBJ := $(GEN)/obj
GEN_SRC := $(GEN)/src

TARGET := pd.apk

.PHONY: all
all: $(GEN)/$(TARGET)

include ./common/include.mk
PD_COMMON_JAR := common/$(PD_COMMON_JAR)
PD_COMMON_RES := common/$(PD_COMMON_RES)

$(PD_COMMON_JAR) $(PD_COMMON_RES):
	@make -C common

.PHONY: R
R: $(RES) $(PD_COMMON_RES)
	@-mkdir -p $(GEN_SRC)
	@echo "Generating R ..."
	@$(AAPT) package $(AAPT_FLAGS)	\
		--extra-packages $(PD_COMMON_R_PACKAGE)	\
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
	@$(AAPT) package $(AAPT_FLAGS)	\
		-I $(AJAR)	\
		-S $(RES)	\
		-S $(PD_COMMON_RES)	\
		-F $(GEN)/pd.ap1
	@cd $(GEN) && $(AAPT) add pd.ap1 $(DEX)
	@echo "Aligning ..."
	@$(ADT_BUILD)/zipalign -f 4 $(GEN)/pd.ap1 $(GEN)/pd.ap2
	@echo "Signing ..."
	@jarsigner	\
		-tsa http://timestamp.digicert.com	\
		-keystore $(KEYSTORE)	\
		-storepass android	\
		-signedjar $@ $(GEN)/pd.ap2 $(CERT)

.PHONY: install
install:
	@adb install -r $(GEN)/$(TARGET)

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN)
	@make -C common clean

