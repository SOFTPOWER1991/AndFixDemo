package www.geek.andfixdemo;

import android.app.Application;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;

import java.io.File;

/**
 * File Description  :  AndFixDemo Application
 *
 * @author : zhanggeng
 * @version : v1.0
 *          **************修订历史*************
 * @email : zhanggengdyx@gmail.com
 * @date : 16/1/16 10:29
 */
public class AppContext extends Application {

    String apatch_path = "/out.apatch";
    String path_all = "";

    private PatchManager patchManager;

    @Override
    public void onCreate() {
        super.onCreate();

        patchManager = new PatchManager(this);
        patchManager.init(BuildConfig.VERSION_CODE + "");//current version

        patchManager.loadPatch();

        try {
//            path_all = Environment.getExternalStorageDirectory().getAbsolutePath() + apatch_path;

            path_all = "/sdcard" + apatch_path;
            Log.e("path", path_all);

            File file = new File(path_all);
            if (file.exists()) {
                Log.e("path exists" , file.getPath());
                patchManager.addPatch(path_all);
            } else {
                Log.e("path unexists", path_all + "========");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
