# Essentia "classic" engine: the same mkxp-z, built against Ruby 1.8.7
# (the Ruby that RPG Maker XP used), for very old fan games.
# Only built when build_ruby18.sh produced ruby18-out/.

ESSENTIA_LEGACY_DIR := $(call my-dir)
ESSENTIA_RUBY18 := $(ESSENTIA_LEGACY_DIR)/ruby18-out

include $(CLEAR_VARS)
LOCAL_MODULE := ruby18
LOCAL_SRC_FILES := $(ESSENTIA_RUBY18)/lib/libruby18.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := ruby18ext
LOCAL_SRC_FILES := $(ESSENTIA_RUBY18)/lib/libruby18ext.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_PATH := $(ESSENTIA_LEGACY_DIR)/mkxp-z
LOCAL_MODULE := mkxp-z-legacy
LOCAL_CPPFLAGS := $(ESSENTIA_MKXPZ_FLAGS) -DMKXPZ_LEGACY_RUBY -fwrapv \
	-Wno-register -Wno-deprecated-register -Wno-c++11-narrowing -Wno-writable-strings
LOCAL_CFLAGS := -fwrapv -ffunction-sections -fdata-sections -fvisibility=hidden
LOCAL_LDFLAGS := $(ESSENTIA_MKXPZ_LDFLAGS)
# Ruby's headers go last: its util.h must not hide mkxp-z's util.h
# (ruby.h finds its own config.h/defines.h next to itself)
LOCAL_C_INCLUDES := $(ESSENTIA_LEGACY_DIR)/build-$(TARGET_ARCH_ABI)/include $(ESSENTIA_MKXPZ_INC) $(ESSENTIA_RUBY18)/include
LOCAL_SRC_FILES := $(ESSENTIA_MKXPZ_SRC)
LOCAL_SHARED_LIBRARIES := SDL2 SDL2_ttf SDL2_image SDL2_sound openal
# ruby18ext needs symbols from ruby18, so it goes first
LOCAL_WHOLE_STATIC_LIBRARIES := ruby18ext ruby18
LOCAL_STATIC_LIBRARIES := libogg libvorbis libtheora physfs pixman uchardet libiconv openssl
LOCAL_LDLIBS := -lz -llog -ldl -lm -lOpenSLES
include $(BUILD_SHARED_LIBRARY)
