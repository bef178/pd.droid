####

AAPT := $(ADT_HOME)/build-tools/19.0.0/aapt
AJAR := $(ADT_HOME)/platforms/android-19/android.jar

GEN_PATH := ../gen

LOCAL_PATH := $(call my-dir)

.PHONY: all clean common-R common-widget-R

all: common-R

common-R: common-widget-R
	@echo -n "Making $@ ... "
	@$(AAPT) package -f -m -J $(GEN_PATH) -I $(AJAR)	\
		-M src/th/common/AndroidManifest.xml	\
		-S res/mime
	@echo "done"

common-widget-R:
	@echo -n "Making $@ ... "
	@$(AAPT) package -f -m -J $(GEN_PATH) -I $(AJAR)	\
		-M src/th/common/widget/AndroidManifest.xml	\
		-S res/pageheader -S res/progressarc
	@echo "done"

clean:
	@echo -n "Cleaning ... "
	@-rm $(addprefix $(GEN_PATH)/th/common/, R.java widget/R.java)
	@echo "done"
