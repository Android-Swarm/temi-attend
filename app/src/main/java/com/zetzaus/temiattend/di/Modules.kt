package com.zetzaus.temiattend.di

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import com.zetzaus.temiattend.R
import com.zetzaus.temiattend.database.AppDatabase
import com.zetzaus.temiattend.database.Attendance
import com.zetzaus.temiattend.face.AzureFaceManager
import com.zetzaus.temiattend.face.MaskDetector
import com.zetzaus.temiattend.ui.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ApplicationComponent::class)
@Module
class Modules {

    @Provides
    fun provideAttendanceDao(@ApplicationContext context: Context) =
        AppDatabase.getInstance(context).attendanceDao

    @Provides
    fun provideVisitorDao(@ApplicationContext context: Context) =
        AppDatabase.getInstance(context).visitorDao

    @Provides
    fun provideAzureFaceManager(@ApplicationContext context: Context) =
        AzureFaceManager(
            endpoint = context.getString(R.string.azure_endpoint),
            apiKey = context.getString(R.string.azure_key)
        )
}

@InstallIn(FragmentComponent::class)
@Module
class FragmentModules {
    @Provides
    fun provideAttendanceDiffUtilCallback() = object : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance) =
            oldItem == newItem
    }
}

@InstallIn(ActivityComponent::class)
@Module
class MainActivityModules {

    @Provides
    fun provideFaceDetector() = FaceDetectorOptions.Builder()
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .build().run {
            FaceDetection.getClient(this)
        }

    @Provides
    fun provideMaskDetector(): MaskDetector {
        // Load custom TFlite Model
        val localModel = LocalModel.Builder()
            .setAssetFilePath(MainActivity.MODEL_FILENAME)
            .build()

        val customOptions = CustomObjectDetectorOptions.Builder(localModel)
            .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
            .enableClassification()
            .build()

        return MaskDetector(customOptions)
    }

    @Provides
    fun provideWifiManager(@ApplicationContext context: Context) =
        ContextCompat.getSystemService(context, WifiManager::class.java)!!

    @Provides
    fun provideConnectivityManager(@ApplicationContext context: Context) =
        ContextCompat.getSystemService(context, ConnectivityManager::class.java)!!

}