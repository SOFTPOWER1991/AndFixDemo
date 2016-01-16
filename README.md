ndroid HotFix —— AndFix使用说明

项目以飞快的速度迭代，2周进行一次迭代升级。每次开发完功能跑通，在现有机型上测试没问题的话，就提交市场。

在以极快的速度迭代的时候，避免不了出现各种问题，传说中的bug
> 或，重大bug，需要紧急修复
> 或，可以下次迭代修复的bug
> 或，影响用户体验的行为

出现bug后，我们的一贯做法：

>发布紧急版本，等待用户下载更新。

可是，并不是所有的用户都会下载更新。那该怎么办？

>强制更新，这是不太友好的行为，如果人现在用的是流量，强制别人更新，会耗费别人流量，还是挺贵的！这确实有点说不过去。

有没有一种方法，在用户不下载新App的前提下修复这些bug。当然有，以下是我找到的两种解决方案。

有两种解决方案：

> 1. [Dexposed](https://github.com/alibaba/dexposed)
> 2. [AndFix](https://github.com/alibaba/AndFix)


##Dexposed

Dexposed是基于开源Xposed框架实现的一个Android平台上功能强大的无侵入式运行时AOP框架。

Dexposed的AOP实现是完全无侵入式的，没有使用任何注解处理器，编织器或者字节码重写器。只需要在应用初始化阶段加载一个很小的JNI库就可以完成Dexposed框架的集成。

Dexposed不仅可以hook应用中的自定义函数，也可以hook你的应用进程中调用的Android框架的函数。这个特性对于Android开发者来说有很多好处，因为我们严重依赖于Android SDK的版本碎片化。

借助动态类加载技术，运行中的app可以加载一小段经过编译的Java AOP代码，在不需要重启app的前提下实现修改目标app的行为。

*典型的应用场景*

> AOP编程
> 插桩（例如测试，性能监控等）
> 在线热更新，修复严重的，紧急的或者安全性的bug
> SDK hooking以提供更好的开发体验


在这里，我只关心Dexposed在Android平台上的*在线热更新`Hotfix`*的使用：

> 在Android平台，Dexposed支持函数级别的在线热更新，例如对已经发布在应用市场上的APK，当我们从crash统计平台上发现某个函数调用有bug，导致经常性crash，而这个时候离下次迭代的预定发布日期还有一段时间。这种情况下，可以在本地开发一个补丁APK，并发布到服务器中，应用市场上的APK下载这个补丁APK并集成后，就可以修复这个crash。

上述涉及到两个角色：

> 1. 宿主apk——已经发布在应用市场上，有bug并且需要修复的apk;
> 2. 补丁apk——在本地开发的，修复了bug的apk;

###HotFix是什么

> HotFix用来修复线上严重的，紧急的或者安全性的bug，这里会涉及到两个apk文件：一个我们称为*宿主apk*，也就是发布到应用市场的apk，一个称为*补丁apk*。*宿主apk*出现bug时，通过在线下载的方式从服务器下载*补丁apk*，使用*补丁apk*中的函数替换原来的函数，从而实现在线修复bug的功能。


###如何借助Dexposed实现HotFix

> 1. 需要再引入一个名为patchloader的jar包,这个函数库实现了一个热更新框架，宿主apk在发布时会将这个jar包一起打包进apk中
> 2. 补丁apk只是在编译时需要这个jar包，但打包成apk时不包含这个jar包，以免补丁apk集成到宿主apk中时发生冲突
> 3. 补丁apk将会以provided的形式依赖dexposedbridge.jar和patchloader.jar,补丁apk的build.gradle文件中依赖部分脚本如下所示

```
dependencies {
    provided files('libs/dexposedbridge.jar')
    provided files('libs/patchloader.jar')
}
```

####支持状态：

> Dexposed支持从Android2.3到4.4（除了3.0）的所有dalvid运行时arm架构的设备，稳定性已经经过实践检验。

####缺点：

> 我的设备是5.0 或者更高系统的使用起来就有点问题了。

因此，我并没打算仔细的学习Dexposed，只是做一个简要的了解。

具体使用可以参见这个博客，写的很棒：

使用示例：[Android平台免Root无侵入AOP框架Dexposed使用详解](http://www.jianshu.com/p/14edcb444c51)

##AndFix

[AndFix](https://github.com/alibaba/AndFix)

### AndFix is What ?

经过综合对比，发现AndFix可用性比较强，因此决定学习AndFix的使用，从官方文档入手。

> AndFix是一个Android App的在线热补丁框架。使用此框架，我们能够在不重复发版的情况下，在线修改App中的Bug。AndFix就是 “Android Hot-Fix”的缩写。
> 它支持Android 2.3到6.0版本，并且支持arm 与 X86系统架构的设备。完美支持Dalvik与ART的Runtime。AndFix 的补丁文件是以 .apatch 结尾的文件。它从你的服务器分发到你的客户端来修复你App的bug 。


原理：

AndFix执行的原理是实现方法体的替换，如下图所示：

方法替换：

AndFix通过Java的自定义注解来判断一个方法是否应该被替换，如果可以就会hook该方法并进行替换。AndFix在ART架构上的Native方法是`art_replaceMethod` 、在X86架构上的Native方法是`dalvik_replaceMethod`。他们的实现方式是不同的。对于Dalvik，它将改变目标方法的类型为`Native`同时hook方法的实现至AndFix自己的`Native`方法，这个方法称为 `dalvik_dispatcher`,这个方法将会唤醒已经注册的回调，这就是我们通常说的`hooked`（挂钩）。对于ART来说，我们仅仅改变目标方法的属性来替代它。

更多信息，见这里：[AndFix/jni/](https://github.com/alibaba/AndFix/tree/master/jni)

###Fix 流程，大致如下：

> 1. 发现bug(友盟、TestIn 、用户反馈)
> 2. 分析bug产生原因
> 3. 创建并发布补丁
> 4. App打补丁

###在App中引入AndFix

####如何来获得AndFix ？

将AndFix aar 作为library编译入你的工程:

* 如果你用的是maven依赖，添加如下代码：

```
<dependency>
    <groupId>com.alipay.euler</groupId>
    <artifactId>andfix</artifactId>
    <version>0.3.1</version>
    <type>aar</type>
</dependency>
```

* 如果你用的是gradle依赖，添加如下代码：

```
dependencies {
    compile 'com.alipay.euler:andfix:0.3.1@aar'
}
```

####如何使用AndFix ？

1. 初始化 PatchManager

	```
		patchManager = new PatchManager(context);
		patchManager.init(appversion);//current version
	```
	
2. Load patch 加载补丁

	```
		patchManager.loadPatch();
	```
	
	你应该尽可能早的加载补丁，通常都是在Application的onCreate()方法中进行初始化。

3. Add patch 添加补丁

	```
		patchManager.addPatch(path);//path:补丁文件下载到本地的路径。
	```
	
	当一个新的补丁文件被下载后，调用addPatch(path)就会立即生效。
	
####开发工具

AndFix 提供了一个补丁创建工具 [apkpatch](https://github.com/alibaba/AndFix/raw/master/tools/apkpatch-1.0.3.zip).

####如何使用这个工具

> 1.准备两个应用apk: 一个是线上的apk，另一个是修复了bug的apk.
> 2.通过提供的两个apk生成补丁文件`.   `

运行如下命令：

```
usage: apkpatch -f <new> -t <old> -o <output> -k <keystore> -p <***> -a <alias> -e <***>
 -a,--alias <alias>     keystore entry alias.
 -e,--epassword <***>   keystore entry password.
 -f,--from <loc>        new Apk file path.
 -k,--keystore <loc>    keystore path.
 -n,--name <name>       patch name.
 -o,--out <dir>         output dir.
 -p,--kpassword <***>   keystore password.
 -t,--to <loc>          old Apk file path.
```

这个时候，你就有了你的应用的救世主——补丁文件。接下来你就可以通过某种方式将该补丁文件分发给你的客户端。

* 或，adb push 到sdk
* 或，发布到服务器，客户端下载该apatch



有时候，你的团队成员可能会fix各自的bug,这个时候就会有不止一个补丁文件`.apatch`。这种情况下，你可以用这个工具merge这些`.apatch`文件：

```
usage: apkpatch -m <apatch_path...> -o <output> -k <keystore> -p <***> -a <alias> -e <***>
 -a,--alias <alias>     keystore entry alias.
 -e,--epassword <***>   keystore entry password.
 -k,--keystore <loc>    keystore path.
 -m,--merge <loc...>    path of .apatch files.
 -n,--name <name>       patch name.
 -o,--out <dir>         output dir.
 -p,--kpassword <***>   keystore password.
```

####运行示例

> 1. 导入samplesI/AndFixDemo进你的IDE，给AndFixDemo添加AndFix（library project 或者 aar）的依赖。
> 2.	build 工程，保存应用为1.apk，然后将该apk安装到手机或者模拟器上。
> 3. 修改com.euler.test.A，作为Fix后的包。
> 4. build 工程，保存应用为2.apk
> 5. Use apkpatch tool to make a patch. 使用apkpatch 工具制作一个补丁文件。
> 6. Rename the patch file to out.apatch, and then copy it to sdcard.重命名补丁文件为out.apatch ，然后将它拷贝到sdcard上。
> 7. 运行1.apk然后查看log

####注意

混淆


如果你使用混淆，你要保存mapping.txt，这样的话你在新版本的构建是就可以借助 “"-applymapping” 来使用它了。

混淆时需要保留下面的内容：

* Native方法 ，比如：`com.alipay.euler.andfix.AndFix`
* Annotation，比如：`com.alipay.euler.andfix.annotation.MethodReplace`

为了确保这些类在运行了ProGuard后，可以被找见，在ProGuard配置中添加如下代码：

```
-keep class * extends java.lang.annotation.Annotation
-keepclasseswithmembernames class * {
    native <methods>;
}
```

####局限性

如果你使用了，软件加固比如 Bangcle，为了生成补丁文件，你最好使用为经过加固的apk.使用这些加固，很可能使热补丁失效。

####安全性

>	验证apatch文件的签名是否就是在使用apkpatch工具时使用的签名（如果不验证那么任何人都可以制作自己的apatch文件来对你的APP进行修改）

>  验证optimize file的指纹（防止有人替换掉本地保存的补丁文件，所以要验证MD5码）


***

以下是我在用demo时碰到的问题：

* 运行apkpatch报nullPointException

	关于该问题的github issue : [运行apkpatch报nullPointException](https://github.com/alibaba/AndFix/issues/45)
	
	解决方法：
	
	> 运行命令行时 alias 填错了，应该填alias名字，我填成了alias路径。修改后，解决了问题。
	
	```
	zhanggeng:apkpatch-1.0.3$
	./apkpatch.sh 
	-f /Users/zhanggeng/Desktop/after/app-release.apk 
	-t /Users/zhanggeng/Desktop/before/app-release.apk 
	-o /Users/zhanggeng/Desktop/apatch/ 
	-k /Users/zhanggeng/Desktop/fixdemo.jks
	-p 123456 
	-a fixdemo 
	-e 123456
	```
	
* 运行程序，检测是否可以使用时，抛出异常：java.util.zip.ZipException: File too short to be a zip file: 0

	关于该问题的github issue: [java.util.zip.ZipException: File too short to be a zip file: 0](https://github.com/alibaba/AndFix/issues/12)
	
	解决方法：
	
	> 这个问题是，上面那个问题造成的。只要补丁文件生成正确，是不会有这个问题的。
	
	
	
	


















