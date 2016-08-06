########################################
# f.mk

#ANDROID_TARGET_SDK ?= $(shell ls $(ANDROID_HOME)/platforms | sort -nr -k 2 -t - | head -1)
ANDROID_TARGET_SDK ?= android-19

ANDROID_BUILD_TOOLS ?= $(shell ls $(ANDROID_HOME)/build-tools | sort -nr | head -1)

########

ANDROID_JAR := $(ANDROID_HOME)/platforms/$(ANDROID_TARGET_SDK)/android.jar
$(info using $(ANDROID_JAR))

ANDROID_BUILD_TOOLS := $(ANDROID_HOME)/build-tools/$(ANDROID_BUILD_TOOLS)
$(info using $(ANDROID_BUILD_TOOLS))

AAPT := $(ANDROID_BUILD_TOOLS)/aapt
DX   := $(ANDROID_BUILD_TOOLS)/dx

########

define sign_jar
    @jarsigner \
        $(shell if test "$(3)" != "false"; then echo "-tsa http://timestamp.digicert.com"; fi) \
        -digestalg SHA1 -sigalg MD5withRSA \
        -keystore $(ANDROID_KEYSTORE) -storepass $(ANDROID_KEYSTORE_PASS) \
        -signedjar $(2) -sigfile cert $(1) $(ANDROID_KEYSTORE_ALIAS)
endef

define sign_apk
    @$(ANDROID_BUILD_TOOLS)/zipalign -f 4 $(1) $(1).aligned
    $(call sign_jar,$(1).aligned,$(2),$(3))
endef

define find_typef
    $(shell find -L $(2) -type f -name $(strip $(1)) -and -not -name ".*")
endef

define do_assign_if_not_yet
	ifndef $(1)
		$(1) := $(2)
	endif
	ifeq ($($(1)),)
		$(1) := $(2)
	endif
endef
define assign_if_not_yet
	$(eval $(call do_$(0),$(1),$(2)))
endef
