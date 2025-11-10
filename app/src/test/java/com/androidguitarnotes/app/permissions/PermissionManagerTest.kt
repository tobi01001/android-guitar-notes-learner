package com.androidguitarnotes.app.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for PermissionManager.
 */
class PermissionManagerTest {
    
    private lateinit var context: Context
    private lateinit var permissionManager: PermissionManager
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        permissionManager = PermissionManager(context)
        mockkStatic(ContextCompat::class)
    }
    
    @After
    fun teardown() {
        unmockkStatic(ContextCompat::class)
    }
    
    @Test
    fun `isRecordAudioPermissionGranted returns true when permission is granted`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_GRANTED
        
        assertTrue(permissionManager.isRecordAudioPermissionGranted())
    }
    
    @Test
    fun `isRecordAudioPermissionGranted returns false when permission is denied`() {
        every {
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
        } returns PackageManager.PERMISSION_DENIED
        
        assertFalse(permissionManager.isRecordAudioPermissionGranted())
    }
    
    @Test
    fun `getRecordAudioPermission returns correct permission string`() {
        assertEquals(Manifest.permission.RECORD_AUDIO, permissionManager.getRecordAudioPermission())
    }
}
