#!/usr/bin/env python3
import sys
import os
import subprocess

if len(sys.argv) < 3:
    print("Usage: build-wg-go.py <ANDROID_ARCH_NAME> <DESTDIR> [ANDROID_PACKAGE_NAME]")
    sys.exit(1)

arch_name = sys.argv[1]
dest_dir = sys.argv[2]
pkg_name = sys.argv[3] if len(sys.argv) > 3 and sys.argv[3] else "com.windscribe.vpn"

ndk_bin = r"F:\Gemini\android_build\sdk\ndk\27.2.12479018\toolchains\llvm\prebuilt\windows-x86_64\bin"
go_exe = r"F:\Gemini\go\bin\go.exe"

arch_map = {
    "arm64": ("arm64", os.path.join(ndk_bin, "aarch64-linux-android24-clang.cmd")),
    "arm": ("arm", os.path.join(ndk_bin, "armv7a-linux-androideabi24-clang.cmd")),
    "x86": ("386", os.path.join(ndk_bin, "i686-linux-android24-clang.cmd")),
    "x86_64": ("amd64", os.path.join(ndk_bin, "x86_64-linux-android24-clang.cmd")),
}

if arch_name not in arch_map:
    print(f"Unknown arch: {arch_name}")
    sys.exit(1)

goarch, cc_cmd = arch_map[arch_name]
os.makedirs(dest_dir, exist_ok=True)
out_file = os.path.join(dest_dir, "libwg-go.so")

env = os.environ.copy()
env["CGO_ENABLED"] = "1"
env["GOOS"] = "android"
env["GOARCH"] = goarch
env["CC"] = cc_cmd
env["CGO_CFLAGS"] = "-O3 -DNDEBUG"
env["CGO_LDFLAGS"] = "-Wl,-soname,libwg-go.so -Wl,-z,max-page-size=16384"

cd_tag = "1.5.4"
cd_commit = "69340d151e159d055745450f16c988fba9210634"
ld_flags = (
    f"-s -w "
    f"-X github.com/Control-D-Inc/ctrld/cmd/cli.version={cd_tag} "
    f"-X github.com/Control-D-Inc/ctrld/cmd/cli.commit={cd_commit} "
    f"-X golang.zx2c4.com/wireguard/ipc.socketDirectory=/data/data/{pkg_name}/cache/wireguard "
    f"-buildid="
)

cwd = os.path.dirname(os.path.abspath(__file__))
cmd = [
    go_exe,
    "build",
    "-tags", "linux",
    f"-ldflags={ld_flags}",
    "-v",
    "-trimpath",
    "-buildvcs=false",
    "-o", out_file,
    "-buildmode", "c-shared"
]

print(f"Compiling libwg-go.so for {arch_name} (GOARCH={goarch})...")
res = subprocess.run(cmd, cwd=cwd, env=env)
if res.returncode != 0:
    print(f"go build failed with exit code {res.returncode}")
    sys.exit(res.returncode)

strip_exe = os.path.join(ndk_bin, "llvm-strip.exe")
if os.path.exists(strip_exe):
    subprocess.run([strip_exe, "--strip-all", out_file])

h_file = os.path.splitext(out_file)[0] + ".h"
if os.path.exists(h_file):
    try:
        os.remove(h_file)
    except Exception:
        pass

local_h = os.path.join(cwd, "libwg-go.h")
if os.path.exists(local_h):
    try:
        os.remove(local_h)
    except Exception:
        pass

print(f"Successfully compiled libwg-go.so ({os.path.getsize(out_file)} bytes) to {out_file}")
