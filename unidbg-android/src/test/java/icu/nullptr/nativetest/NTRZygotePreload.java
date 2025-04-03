package icu.nullptr.nativetest;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.arm.backend.Unicorn2Factory;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.DalvikModule;
import com.github.unidbg.linux.android.dvm.DvmClass;
import com.github.unidbg.linux.android.dvm.DvmObject;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.linux.android.dvm.jni.ProxyClassFactory;
import com.github.unidbg.memory.Memory;

import java.io.File;

public class NTRZygotePreload {
    private static final String pkgName = "icu.nullptr.nativetest";
    private static final String libPath = "unidbg-android/src/test/resources/icunullptr/libnullptr.so";
    private static final String JNI_CLASS = "icu/nullptr/nativetest/NTRZygotePreload";

    private final AndroidEmulator emulator;
    private DvmObject<?> dvmObject;
    private final VM vm;
    private final DvmClass mNTRZygotePreLoad;

    private NTRZygotePreload() {
        System.out.println("Tracing ic");
        emulator = AndroidEmulatorBuilder.for64Bit()
                .setProcessName(pkgName)
                .addBackendFactory(new Unicorn2Factory(true))
                .setRootDir(new File("unidbg-android/src/lib64"))
                .build();

        Memory memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(27));

        vm = emulator.createDalvikVM(new File("/Users/10185230/Downloads/NativeTest-v30-1b29f91-ad-release.apk"));
        vm.setDvmClassFactory(new ProxyClassFactory());
        vm.setVerbose(false);

        vm.loadLibrary(new File("unidbg-android/lib64/libc.so"), false);
        vm.loadLibrary(new File("unidbg-android/lib64/libGLESv1_CM.so"), false);
        vm.loadLibrary(new File("unidbg-android/lib64/libEGL.so"), false);
        vm.loadLibrary(new File("unidbg-android/lib64/liblog.so"), false);

        vm.loadLibrary(new File("unidbg-android/lib64/libandroid.so"), false);
        vm.loadLibrary(new File("unidbg-android/lib64/libm.so"), false);
        vm.loadLibrary(new File("unidbg-android/lib64/libdl.so"), false);

        DalvikModule dm = vm.loadLibrary(new File(libPath), true);

        mNTRZygotePreLoad = vm.resolveClass(JNI_CLASS);
        dm.callJNI_OnLoad(emulator);
        System.out.println("mNTRZygotePreLoad => "+mNTRZygotePreLoad);
    }
    //Java_icu_nullptr_nativetest_NTRZygotePreload_check	0000000000A6EAA0

    public static void main(String[] args) {

        NTRZygotePreload ntrZygotePreload = new NTRZygotePreload();
        ntrZygotePreload.check();

    }

    public void check() {
        String methodSig = "check()I";
        int value = mNTRZygotePreLoad.callJniMethodInt(emulator, methodSig);
        System.out.println("mNTRZygotePreLoad: " + mNTRZygotePreLoad + " check =" + value);


    }


}
