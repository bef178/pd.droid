########
# Due to aapt id-generating mechanism, apk is likely to have a same R id as that in jar.
# So provide res and jar ... and also package res in jar.

define all-typef
	`find -L $(2) -type f -name "$(strip $(1))"`
endef

ADT_HOME := $(HOME)/app/android-sdk-linux
AAPT := $(ADT_HOME)/build-tools/22.0.1/aapt
AJAR := $(ADT_HOME)/platforms/android-19/android.jar

GEN := ./gen
RES := ./res_mod
SRC := ./src

GEN_OBJ := $(GEN)/obj
GEN_RES := $(GEN)/res
GEN_SRC := $(GEN)/src

TARGET := pd-common.jar


.PHONY: all
all: $(GEN)/$(TARGET)

.PHONY: R
R:
	@echo "Generating res ..."
	@-mkdir -p $(GEN_RES)
	@cp -r $(RES)/*/* $(GEN_RES)
	@echo "Generating R ..."
	@-mkdir -p $(GEN_SRC)
	@$(AAPT) package	\
		--auto-add-overlay -f -M ./AndroidManifest.xml	\
		--non-constant-id	\
		--custom-package "th.pd.common.android"	\
		-I $(AJAR)	\
		-S $(GEN_RES)	\
		-m -J $(GEN_SRC)

.PHONY: classes
classes: R
	@-mkdir -p $(GEN_OBJ)
	@echo "Compiling ..."
	@javac $(call all-typef, *.java, $(SRC))	\
		-sourcepath $(SRC):$(GEN_SRC)	\
		-classpath $(AJAR)	\
		-d $(GEN_OBJ)

$(GEN)/$(TARGET): classes
	@echo "Packaging ..."
# as lib, remove the R stuff
	@-rm `find $(GEN_OBJ) -regex ".*/R\($$.+\)?\.class"`
	@jar cf $@ -C $(GEN_OBJ) .

.PHONY: clean
clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN)
