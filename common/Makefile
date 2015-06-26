########
# Due to aapt id-generating mechanism, apk is likely to have a same R id as that in jar.
# So provide res and jar ... and also package res in jar.

define all-typef
	`find -L $(2) -type f -name "$(strip $(1))"`
endef

ADT_HOME := $(HOME)/app/android-sdk-linux
AAPT := $(ADT_HOME)/build-tools/22.0.1/aapt
AJAR := $(ADT_HOME)/platforms/android-19/android.jar

GEN_PATH := ./gen
RES_PATH := ./res_mod
SRC_PATH := ./src

GEN_BIN := $(GEN_PATH)/bin
GEN_RES := $(GEN_PATH)/res
GEN_SRC := $(GEN_PATH)/src

TARGET := pd-common.jar

AAPT += package	\
	--auto-add-overlay	\
	-f	\
	-I $(AJAR)	\
	-M ./AndroidManifest.xml	\
	-S $(RES_PATH)/common	\
	-S $(RES_PATH)/mime	\
	-S $(RES_PATH)/pageheader	\
	-S $(RES_PATH)/progressarc	\
	-S $(RES_PATH)/themeClassic

.PHONY: all clean

all: $(TARGET)

R:
	@-mkdir -p $(GEN_SRC)
	@echo "Making common.R ..."
	@$(AAPT) --custom-package "th.pd.common.android"	\
		-m -J $(GEN_SRC)

dot_class: R
	@-mkdir -p $(GEN_BIN)
	@echo "Compiling ..."
	@javac $(call all-typef, *.java, $(SRC_PATH)) $(call all-typef, *.java, $(GEN_SRC))	\
		-sourcepath $(SRC_PATH):$(GEN_SRC)	\
		-classpath $(AJAR)	\
		-d $(GEN_BIN)

$(TARGET): dot_class
	@echo "Packaging ..."
	@$(AAPT)	\
		-F $(GEN_PATH)/$@	\
		$(GEN_BIN)
# generate res to be included in apk
	@echo "Generating res ..."
	@-mkdir -p $(GEN_RES)
	@cp -r $(RES_PATH)/*/* $(GEN_RES)

clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN_PATH)
