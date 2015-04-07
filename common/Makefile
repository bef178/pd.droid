####

define all-typef-with-ext
	`find -L $(2) -type f -name "*.$(strip $(1))"`
endef

AAPT := $(ADT_HOME)/build-tools/19.0.0/aapt
AJAR := $(ADT_HOME)/platforms/android-19/android.jar

GEN_PATH := ./gen
RES_PATH := ./res_mod
SRC_PATH := ./src

GEN_BIN := $(GEN_PATH)/bin
GEN_RES := $(GEN_PATH)/res
GEN_SRC := $(GEN_PATH)/src

TARGET := th-common.jar

AAPT += package	\
	--auto-add-overlay	\
	-f -m -J $(GEN_SRC)	\
	-I $(AJAR)	\
	-M ./AndroidManifest.xml

.PHONY: all clean

all: $(TARGET)

R:
	@-mkdir -p $(GEN_SRC)
	@echo "Making common.R ..."
	@$(AAPT) --custom-package "th.common"	\
		-S $(RES_PATH)/mime
	@echo "Making common.widget.R ..."
	@$(AAPT) --custom-package "th.common.widget"	\
		-S $(RES_PATH)/pageheader	\
		-S $(RES_PATH)/progressarc

dot_class: R
	@-mkdir -p $(GEN_BIN)
	@echo "Compiling ..."
	@javac $(call all-typef-with-ext, java, $(SRC_PATH)) $(call all-typef-with-ext, java, $(GEN_SRC))	\
		-sourcepath $(SRC_PATH):$(GEN_SRC)	\
		-classpath $(AJAR)	\
		-d $(GEN_BIN)

$(TARGET): dot_class
	@echo "Packaging ..."
	@cd $(GEN_BIN) && jar -cf ../$@ $(subst $(GEN_BIN)/, , $(call all-typef-with-ext, class))
	@echo "Generating res ..."
	@-mkdir -p $(GEN_RES)
	@cp -r $(RES_PATH)/*/* $(GEN_RES)

clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN_PATH)
