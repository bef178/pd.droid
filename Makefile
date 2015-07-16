########
# pd

LOCAL_PD_COMMON := ./common
LOCAL_GEN := ./gen
LOCAL_PACKAGE := th.pd
LOCAL_TARGET := $(LOCAL_GEN)/pd.apk

include $(LOCAL_PD_COMMON)/def-apk.mk

########

MAKEFILES := $(wildcard */Makefile)

.PHONY: all-each
all-each: $(addsuffix .done, $(MAKEFILES))

%/Makefile.done: %/Makefile
	make -C $(dir $<)
