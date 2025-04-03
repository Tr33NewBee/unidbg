package com.einnovation.temu;


import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.LibraryResolver;
import com.github.unidbg.arm.backend.DynarmicFactory;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.memory.Memory;

import java.io.File;
import java.io.FileFilter;
import java.util.Objects;

/***
 * 测试temu计算ta-token的算法 temu-v3.41版本的ta-token的生成
 * 输入
 * i4_SecureNative_2.i3 is called: context=com.baogong.WhaleCoApplication@64d4d0, j10=1742783506730, str=0F4uZbecyagX
 * i4_SecureNative_2.i3 result=2aiJ71ltspySpYqKJ5uFeFEwcOjl1eBuYPYSefRInMpUOROCoAsCT99nOamhxC2qcVk
 */
public class SecureNative extends AbstractJni {
    private static final String libPath ="/Users/10185230/Desktop/需要分析的app/temux/341/com.einnovation.temu_3.41.0/lib/arm64-v8a/libriver.so";
    private static final String apk ="/Users/10185230/Desktop/需要分析的app/temux/341/com.einnovation.temu_3.41.0/base.apk";
    private static final String app = "com.einnovation.temu";
    private  Memory memory;
    private VM vm;
    private  DalvikModule   dm;
    AndroidEmulator emulator;
    private SecureNative(){
         emulator = AndroidEmulatorBuilder.for64Bit()
//                .addBackendFactory(new DynarmicFactory(true))
                .setProcessName(app)
                .build();
        //
         memory = emulator.getMemory();
        LibraryResolver resolver = new AndroidResolver(23);
        //
        memory.setLibraryResolver(resolver);
         vm = emulator.createDalvikVM(new File(apk));//new File(apk)
//        vm.(0x00010006);
        vm.setVerbose(true);
        //
//        emulator.getSyscallHandler().setVerbose(false);
//        emulator.getSyscallHandler().setEnableThreadDispatcher(false);
//        emulator.getSyscallHandler().
         //
        vm.setJni(this);
        //
        // 加载指定路径的SO库文件，不自动调用JNI_OnLoad函数
//        log(System.getProperty("user.dir"));
        //fixme 加载下面的so程序就会中断下来，我还不知道为什么。
        String cwd = System.getProperty("user.dir");
//        loadLibraries(vm,"unidbg-android/lib64");
//        vm.loadLibrary(new File(cwd+"/unidbg-android/lib64/libc.so"),false);//加载android系统依赖
//        vm.loadLibrary(new File(cwd+"/unidbg-android/lib64/libdl.so"),false);//加载android系统依赖
//        vm.loadLibrary(new File(cwd+"/unidbg-android/lib64/libm.so"),false);//加载android系统依赖
//        vm.loadLibrary(new File(cwd+"/unidbg-android/lib64/libandroid.so"),false);//加载android系统依赖

        //加载其他的so--不用加载也可以
//        loadLibraries(vm,new File(libPath).getParentFile().getAbsolutePath());
        dm =  vm.loadLibrary(new File(libPath), false); //目标so加载获取handle
        // 手动调用JNI_OnLoad方法
        log("lib base at 0x"+Long.toHexString(dm.getModule().base));
        dm.callJNI_OnLoad(emulator);
        log("===========================done========================");


    }

    public static void main(String[] args) {

        SecureNative secureNative =new SecureNative();
//        secureNative.installHook();
        secureNative.updateAndroidId("");
        secureNative.genTaToken();//调用创建ta-token

    }
    private void installHook(){
        //调试看看i3函数的br x8的第一条call是什么
        log("================installHook================");
        emulator.attach().addBreakPoint(dm.getModule().base + 0x234F04, new BreakPointCallback() {
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {
                log("break point at: "+Long.toHexString(address));
                return true;
            }
        });
    }

    /***
     * 生成ta-token的参数
     */
    private  void genTaToken(){
        //调用static方案
        //fixme：如果是object对象调用，应该如何实现？
        DvmObject<?> application= vm.resolveClass("Landroid/content/Context");//("com.baogong.WhaleCoApplication".replace(".","/"));
        log("application = "+application.getObjectType().getName());
        DvmClass mSecureNative = vm.resolveClass("ri/i4");
        String signature = "i3(Landroid/content/Context;JLjava/lang/String;)Ljava/lang/String;";
        log("current time: "+System.currentTimeMillis());
        //  j10=1742783506730, str=0F4uZbecyagX
        StringObject obj = mSecureNative.callStaticJniMethodObject(emulator,signature,application ,System.currentTimeMillis(),"0F4uZbecyagX");
//        Module module =emulator.getMemory().findModule("libriver.so");
//        log(module.name);
//        module.callFunction(emulator,0x233d7c,vm.getJNIEnv(),);
        if(obj != null)
            log(obj.toString());
    }
    private static void log(String msg){
        System.out.println("[Temu]: "+msg);
    }

    private void loadLibraries(VM vm,String path){
        if(Objects.equals(path, "")){
            log("error: load  libraries failed. ");
            return;
        }
        File[] files = new File(path).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().endsWith(".so") && !file.getName().equals("libriver.so") ;
            }
        });
        for(File file: files){
//            if(file.getName().equals("libssl.so"))continue;
            log("loaded lib: "+file.getName());
            vm.loadLibrary(file,false);
        }
    }
}
