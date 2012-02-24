package com.primer.world.android;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxFileSizeException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

public class ExportCsvFileToDropBox extends AsyncTask<Void, Long, Boolean> {

	private Context mContext = null;
	private DropboxAPI<?> mApi = null;
	private File mFile = null;
	private String mFileName = null;
	private long mFileLength = 0;
	private UploadRequest mRequest = null;
	private ProgressDialog mDialog = null;
	private String mErrorMsg = null;
	private static final String DROP_BOX_DIRECTORY = "WordBooks";

	public ExportCsvFileToDropBox(Context context, DropboxAPI<?> api,
											WordBook wordBook){
		// そもそも指定したディレクトリ(WordBook)が存在しない可能性があるので、作成
		List<Entry> entries = null;
		try {
			// ルートパスにあるディレクトリを100個まで探す
			entries = api.search("", DROP_BOX_DIRECTORY, 100, false);
		} catch (DropboxException e) {
			e.printStackTrace();
		}
		if (entries.isEmpty()) { // 所定のディレクトリがないので作成する
			try {
				api.createFolder(DROP_BOX_DIRECTORY);
			} catch (DropboxException e) {
				// Couldn't make directory properly.
				Utils.showToast(context, e.toString());
				return;
			}
		}

		// jp:ここから先には、親ディレクトリが存在していなければならない
		// en:From now on, there should be a parent directory in Dbox.
		mContext = context;
		mApi = api;

		//DropBoxに出力するときは、名称を単語帳のままにする。
		mFileName = wordBook.getWordBookName();
		//しかし、SDに一時的に出力する際は、名称に_tempをつける。
		wordBook.setWordBookName(mFileName + "_temp");
		// First of all, you should export word book to SD
		mFile = Utils.exportWordBookToSD(context, wordBook);

		// Finished exporting to local memory.
		// ローカルへの出力が完了したところで、次はそれをdropboxに移す作業。
		mFileLength = mFile.length();

		mDialog = new ProgressDialog(context);
		mDialog.setMax(100);
		mDialog.setMessage("Exporting " + mFile.getName());
		mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mDialog.setProgress(0);
		mDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// This will cancel the putFile operation
				mRequest.abort();
			}
		});
		mDialog.show();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		try {
			// By creating a request, we get a handle to the putFile
			// operation, so we can cancel it later if we want to
			FileInputStream fis = new FileInputStream(mFile);
			String path = DROP_BOX_DIRECTORY + "/" + mFileName + ".csv";
			mRequest = mApi.putFileOverwriteRequest(path, fis, mFile.length(),
					new ProgressListener() {
						@Override
						// Update the progress bar every half-second or so
						public long progressInterval() {
							return 500;
						}

						@Override
						public void onProgress(long bytes, long total) {
							publishProgress(bytes);
						}
					});
			if (mRequest != null) {
				// upload a file to dropbox
				mRequest.upload();
				return true;
			}
		} catch (DropboxUnlinkedException e) {
			// This session wasn't authenticated properly or user unlinked
			mErrorMsg = "This app wasn't authenticated properly.";
		} catch (DropboxFileSizeException e) {
			// File size too big to upload via the API
			mErrorMsg = "This file is too big to upload";
		} catch (DropboxPartialFileException e) {
			// We canceled the operation
			mErrorMsg = "Upload canceled";
		} catch (DropboxServerException e) {
			// Server-side exception.
			if (e.error == DropboxServerException._401_UNAUTHORIZED) {
				// Unauthorized, so we should unlink them.
				// You may want to automatically log the user out
				// in this case.
			} else if (e.error == DropboxServerException._403_FORBIDDEN) {
				// Not allowed to access this
			} else if (e.error == DropboxServerException._404_NOT_FOUND) {
				// path not found (or if it was the thumbnail, can't be
				// thumbnailed)
			} else if (e.error == DropboxServerException
					                 ._507_INSUFFICIENT_STORAGE) {
				// user is over quota
			} else {
				// Something else
			}
			// This gets the Dropbox error, translated into the user's
			// language
			mErrorMsg = e.body.userError;
			if (mErrorMsg == null) {
				mErrorMsg = e.body.error;
			}
		} catch (DropboxIOException e) {
			// Happens all the time, probably want to retry automatically
			mErrorMsg = "Network error.  Try again.";
		} catch (DropboxParseException e) {
			// Probably due to Dropbox server restarting, should retry
			mErrorMsg = "Dropbox error.  Try again.";
		} catch (DropboxException e) {
			// Unknown error
			mErrorMsg = "Unknown error.  Try again.";
		} catch (FileNotFoundException e) {
			// Couldn't find the file
		}
		return false;
	}

	@Override
	protected void onProgressUpdate(Long... progress) {
		int percent = (int) (100.0 * (double) progress[0] / mFileLength + 0.5);
		mDialog.setProgress(percent);
	}

	@Override
	protected void onPostExecute(Boolean result) {
		mDialog.dismiss();
		if (result) {
			showToast("File successfully uploaded");
		} else {
			showToast(mErrorMsg);
		}
		if(mFile != null) {
		// Delete the file which was made locally.(ローカル保存したデータ削除)
		// This should be done completely.	
			if( !mFile.delete()){
				Utils.showToast(mContext, 
						R.string.export_csv_del_file_error);
			}
		}
		((Activity)mContext).finish();
	}

	private void showToast(String msg) {
		Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
		error.show();
	}
}