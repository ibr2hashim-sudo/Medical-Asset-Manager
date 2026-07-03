package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AssetDao {
    @Query("SELECT * FROM assets ORDER BY id DESC")
    fun getAllAssets(): Flow<List<MedicalAsset>>

    @Query("SELECT * FROM assets WHERE department = :dept ORDER BY id DESC")
    fun getAssetsByDepartment(dept: String): Flow<List<MedicalAsset>>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    fun getAssetById(id: Long): Flow<MedicalAsset?>

    @Query("SELECT * FROM assets WHERE id = :id LIMIT 1")
    suspend fun getAssetByIdSync(id: Long): MedicalAsset?

    @Query("""
        SELECT * FROM assets 
        WHERE assetName LIKE '%' || :query || '%' 
           OR assetId LIKE '%' || :query || '%' 
           OR model LIKE '%' || :query || '%' 
           OR department LIKE '%' || :query || '%' 
           OR serialNumber LIKE '%' || :query || '%'
        ORDER BY id DESC
    """)
    fun searchAssets(query: String): Flow<List<MedicalAsset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: MedicalAsset): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<MedicalAsset>)

    @Update
    suspend fun updateAsset(asset: MedicalAsset)

    @Delete
    suspend fun deleteAsset(asset: MedicalAsset)

    @Query("DELETE FROM assets WHERE id = :id")
    suspend fun deleteAssetById(id: Long)

    @Query("DELETE FROM assets WHERE department = :dept")
    suspend fun deleteAssetsByDepartment(dept: String)

    @Query("DELETE FROM assets")
    suspend fun clearAllAssets()
}
