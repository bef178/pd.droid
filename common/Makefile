####

AAPT := $(ADT_HOME)/build-tools/19.0.0/aapt
AJAR := $(ADT_HOME)/platforms/android-19/android.jar

LOCAL_PATH := $(call my-dir)
GEN_PATH := ./gen
BIN_PATH := ./bin

TARGET := th-common.jar

AAPT += package	\
	--auto-add-overlay	\
	-f -m -J $(GEN_PATH)	\
	-I $(AJAR)	\
	-M ./AndroidManifest.xml

.PHONY: all clean

all: $(TARGET)

R:
	@-mkdir -p $(GEN_PATH)
	@echo "Making common.R ..."
	@$(AAPT) --custom-package "th.common"	\
		-S res/mime
	@echo "Making common.widget.R ..."
	@$(AAPT) --custom-package "th.common.widget"	\
		-S res/pageheader	\
		-S res/progressarc

java: R
	@-mkdir -p $(BIN_PATH)
	@echo "Compiling ..."
	@javac `find . -name *.java`	\
		-sourcepath gen:res	\
		-classpath $(AJAR)	\
		-d $(BIN_PATH)

$(TARGET): java
	@echo "Packaging ..."
	@cd $(BIN_PATH) && jar -cf $@ `find . -name *.class`

clean:
	@echo "Cleaning ... "
	@-rm -rf $(GEN_PATH) $(BIN_PATH)
