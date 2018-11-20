package fans.skin.expresstrade.managers.network;

import android.os.*;

import com.squareup.okhttp.*;

import java.io.*;

import okio.*;

public class ProgressRequestBody extends RequestBody {
    private File mFile;
    private String mPath;
    private UploadCallbacks mListener;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public interface UploadCallbacks {
        void onProgressUpdate(int percentage);
    }

    public ProgressRequestBody(final File file, final UploadCallbacks listener) {
        mFile = file;
        mListener = listener;
    }

    @Override
    public MediaType contentType() {
        // i want to upload only images
        return MediaType.parse("image/jpg");
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(mFile);
        long uploaded = 0;

        try {
            int read;
            Handler handler = new Handler(Looper.getMainLooper());
            while ((read = in.read(buffer)) != -1) {

                // notifyItems progress_tile_bitmap_1 on UI thread
                handler.post(new ProgressUpdater(uploaded, fileLength));

                uploaded += read;
                sink.write(buffer, 0, read);
            }
        } finally {
            in.close();
        }
    }

    private class ProgressUpdater implements Runnable {
        private long mUploaded;
        private long mTotal;

        public ProgressUpdater(long uploaded, long total) {
            mUploaded = uploaded;
            mTotal = total;
        }

        @Override
        public void run() {
            mListener.onProgressUpdate((int) (100 * mUploaded / mTotal));
        }
    }
}