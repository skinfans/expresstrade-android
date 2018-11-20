package fans.skin.expresstrade.managers.network;

import android.os.*;
import android.util.*;

import java.io.*;
import java.net.*;

public class NetworkDownload {
    // startDownload();

    public void startDownload(String url) {
        Log.d("1234", "start download");
        new DownloadFileAsync().execute(url);
    }

    private class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... u) {
            int count;

            try {

                URL url = new URL(u[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                int lengthOfFile = connection.getContentLength();
                Log.d("1234", "Length of file: " + lengthOfFile);

                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/MyZip.zip");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress((int) ((total * 100) / lengthOfFile) + "");
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                Log.d("1234", "complete");
                Log.d("1234", Environment.getExternalStorageDirectory().getAbsoluteFile() + "/swinechat/stickers/MyZip.zip");

            } catch (Exception e) {
                Log.d("1234", "error");
                System.out.println(e);
            }
            return null;

        }

        protected void onProgressUpdate(String... progress) {
            Log.d("1234", progress[0]);
            // progress_tile_bitmap_1: Integer.parseInt(progress_tile_bitmap_1[0])
        }

        @Override
        protected void onPostExecute(String unused) {
            // close DIALOG_DOWNLOAD_PROGRESS
        }
    }
}