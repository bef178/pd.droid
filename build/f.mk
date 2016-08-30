########################################
# f.mk
#
# ANDROID_HOME
# KEYSTORE
# KEYSTORE_PASS
# KEY_NAME

#ANDROID_API ?= $(shell ls $(ANDROID_HOME)/platforms | sort -nr -k 2 -t - | head -1)
ANDROID_API ?= android-22

ANDROID_BUILD_BIN ?= $(shell ls $(ANDROID_HOME)/build-tools | sort -nr | head -1)

########

ANDROID_JAR := $(ANDROID_HOME)/platforms/$(ANDROID_API)/android.jar
$(info using $(ANDROID_JAR))

ANDROID_BUILD_BIN := $(ANDROID_HOME)/build-tools/$(ANDROID_BUILD_BIN)
$(info using $(ANDROID_BUILD_BIN))

AAPT := $(ANDROID_BUILD_BIN)/aapt
DX   := $(ANDROID_BUILD_BIN)/dx

########

define sign_jar
    @jarsigner \
        $(shell if test "$(3)" != "true"; then echo "-tsa http://timestamp.digicert.com"; fi) \
        -digestalg SHA1 -sigalg MD5withRSA \
        -keystore $(KEYSTORE) -storepass $(KEYSTORE_PASS) \
        -signedjar $(2) -sigfile cert $(1) $(KEY_NAME)
endef

define sign_apk
    $(call sign_jar,$(1),$(2).signed,$(3))
    @$(ANDROID_BUILD_BIN)/zipalign -f 4 $(1) $(2)
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
