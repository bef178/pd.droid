########################################
#

sign_apk() {
    local opts=
    opts=$(getopt -o "" -l verbose,store_name:,store_pass:,key_name: -- "$@")
    if test $? != 0; then
        exit 1
    fi
    eval set -- "$opts"

    local verbose=
    local store_name=
    local store_pass=
    local key_name=
    while true ; do
        case "$1" in
            --verbose)
                verbose=1
                shift
                ;;
            --store_name)
                store_name="$2"
                shift 2
                ;;
            --store_pass)
                store_pass="$2"
                shift 2
                ;;
            --key_name)
                key_name="$2"
                shift 2
                ;;
            --)
                shift
                break
                ;;
            *)
                break
                ;;
        esac
    done
    local src=$1
    local dst=$2

    if test -n "$verbose"; then
        echo "args:"
        echo "  store: [$store_name] [$store_pass] [$key_name]"
        echo "  [$src] [$dst]"
    fi
    if test ! -f "$src"; then
        >&2 echo "E: fail to find src [$src]"
        return 1
    fi
    if test -z "$dst"; then
        echo "W: unspecified dst, assume [$src]"
        dst="$src"
    fi

    local cmd="jarsigner"

    if ping -c 2 -W 1 www.baidu.com >/dev/null 2>&1; then
        local ntpServer="http://timestamp.digicert.com"
        echo "Using $ntpServer"
        cmd="$cmd -tsa $ntpServer"
    fi

    cmd="$cmd \
        -digestalg SHA1 -sigalg MD5withRSA -sigfile cert
        -keystore $store_name -storepass $store_pass \
        -signedjar $dst $src $key_name"
    if test -n "$verbose"; then
        echo $cmd
    fi
    if ! $cmd; then
        >&2 echo "E: fail to sign [$src]"
        exit 1;
    fi
    echo "[$dst] signed."
    if ! align_apk "$dst"; then
        exit 1;
    fi
    echo "[$dst] aligned."
}

# usage: align_zip path-to-zip
align_apk() {
    if test -z "$(which zipalign)"; then
        >&2 echo "E: fail to find zipalign"
        return 1
    fi

    local dst="$1"
    if test ! -f "$dst"; then
        >&2 echo "E: fail to find target [$dst]"
        return 1
    fi

    local tmp=$(mktemp)
    trap "rm -rf $tmp" EXIT

    cp "$dst" $tmp
    zipalign -f 4 $tmp "$dst"
    if ! zipalign -c 4 "$dst"; then
        >&2 echo "E: fail to align [$dst]"
        return 1
    fi
}

sign_apk $@
