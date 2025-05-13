import com.m7md7sn.capstoneApp.data.model.FirebaseSensorData
import com.m7md7sn.capstoneApp.data.repository.FirebaseSensorRepository
import com.m7md7sn.capstoneApp.data.repository.SensorRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SensorRepositoryTest {
    private lateinit var firebaseRepo: FirebaseSensorRepository
    private lateinit var repo: SensorRepository

    @Before
    fun setup() {
        firebaseRepo = mockk(relaxed = true)
        repo = SensorRepository(firebaseRepo)
    }

    @Test
    fun getSensorReadingsReturnsCorrectData() {
        runTest {
            val fakeData = mapOf(
                "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
            )
            // Simulate the latestData cache
            val repo = SensorRepository(mockk(relaxed = true))
            val latestDataField = SensorRepository::class.java.getDeclaredField("latestData")
            latestDataField.isAccessible = true
            latestDataField.set(repo, fakeData)

            val readings = repo.getSensorReadings()
            assertEquals(4, readings.size)
            assertEquals("100.0", readings[0].value) // TDS
            assertEquals("7.10", readings[1].value) // pH
            assertEquals("2.50", readings[2].value) // Turbidity
            assertEquals("25.0", readings[3].value) // Temperature
        }
    }

    @Test
    fun getSensorReadingsFlowEmitsCorrectData() {
        runTest {
            val fakeData = mapOf(
                "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
            )
            coEvery { firebaseRepo.getLatestSensorData() } returns flowOf(fakeData)
            val repo = SensorRepository(firebaseRepo)
            val result = repo.getSensorReadingsFlow()
            val readings = result.first()
            assertEquals(4, readings.size)
            assertEquals("100.0", readings[0].value)
        }
    }

    @Test
    fun getTimedSensorReadingsReturnsCorrectData() {
        runTest {
            val repo = SensorRepository(mockk(relaxed = true))
            val latestDataField = SensorRepository::class.java.getDeclaredField("latestData")
            latestDataField.isAccessible = true
            val fakeData = mapOf(
                "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
            )
            latestDataField.set(repo, fakeData)
            val readings = repo.getTimedSensorReadings("ph")
            assertTrue(readings.any { it.value == 7.1f })
        }
    }

    @Test
    fun getTimedSensorReadingsFlowEmitsCorrectData() {
        runTest {
            val fakeList = listOf(FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f))
            coEvery { firebaseRepo.getSensorHistory() } returns flowOf(fakeList)
            val repo = SensorRepository(firebaseRepo)
            val result = repo.getTimedSensorReadingsFlow("ph")
            val readings = result.first()
            assertTrue(readings.any { it.value == 7.1f })
        }
    }

    @Test
    fun getSensorStatusesReturnsCorrectData() {
        runTest {
            val repo = SensorRepository(mockk(relaxed = true))
            val latestDataField = SensorRepository::class.java.getDeclaredField("latestData")
            latestDataField.isAccessible = true
            val fakeData = mapOf(
                "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
            )
            latestDataField.set(repo, fakeData)
            val statuses = repo.getSensorStatuses()
            assertEquals(4, statuses.size)
            assertEquals("pH", statuses[0].name)
        }
    }

    @Test
    fun getSensorStatusesFlowEmitsCorrectData() {
        runTest {
            val fakeData = mapOf(
                "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
            )
            coEvery { firebaseRepo.getLatestSensorData() } returns flowOf(fakeData)
            val repo = SensorRepository(firebaseRepo)
            val result = repo.getSensorStatusesFlow()
            val statuses = result.first()
            assertEquals(4, statuses.size)
            assertEquals("pH", statuses[0].name)
        }
    }

    @Test
    fun getSensorReadingsReturnsCorrectData() = runTest {
        val fakeData = mapOf(
            "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
        )
        // Simulate the latestData cache
        val repo = SensorRepository(mockk(relaxed = true))
        val latestDataField = SensorRepository::class.java.getDeclaredField("latestData")
        latestDataField.isAccessible = true
        latestDataField.set(repo, fakeData)

        val readings = repo.getSensorReadings()
        assertEquals(4, readings.size)
        assertEquals("100.0", readings[0].value) // TDS
        assertEquals("7.10", readings[1].value) // pH
        assertEquals("2.50", readings[2].value) // Turbidity
        assertEquals("25.0", readings[3].value) // Temperature
    }

    @Test
    fun getSensorReadingsFlowEmitsCorrectData() = runTest {
        val fakeData = mapOf(
            "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
        )
        coEvery { firebaseRepo.getLatestSensorData() } returns flowOf(fakeData)
        val repo = SensorRepository(firebaseRepo)
        val result = repo.getSensorReadingsFlow()
        val readings = result.first()
        assertEquals(4, readings.size)
        assertEquals("100.0", readings[0].value)
    }

    @Test
    fun getTimedSensorReadingsReturnsCorrectData() = runTest {
        val repo = SensorRepository(mockk(relaxed = true))
        val latestDataField = SensorRepository::class.java.getDeclaredField("latestData")
        latestDataField.isAccessible = true
        val fakeData = mapOf(
            "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
        )
        latestDataField.set(repo, fakeData)
        val readings = repo.getTimedSensorReadings("ph")
        assertTrue(readings.any { it.value == 7.1f })
    }

    @Test
    fun getTimedSensorReadingsFlowEmitsCorrectData() = runTest {
        val fakeList = listOf(FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f))
        coEvery { firebaseRepo.getSensorHistory() } returns flowOf(fakeList)
        val repo = SensorRepository(firebaseRepo)
        val result = repo.getTimedSensorReadingsFlow("ph")
        val readings = result.first()
        assertTrue(readings.any { it.value == 7.1f })
    }

    @Test
    fun getSensorStatusesReturnsCorrectData() = runTest {
        val repo = SensorRepository(mockk(relaxed = true))
        val latestDataField = SensorRepository::class.java.getDeclaredField("latestData")
        latestDataField.isAccessible = true
        val fakeData = mapOf(
            "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
        )
        latestDataField.set(repo, fakeData)
        val statuses = repo.getSensorStatuses()
        assertEquals(4, statuses.size)
        assertEquals("pH", statuses[0].name)
    }

    @Test
    fun getSensorStatusesFlowEmitsCorrectData() = runTest {
        val fakeData = mapOf(
            "1" to FirebaseSensorData(tds = 100f, pH = 7.1f, turbidity = 2.5f, temperature = 25f, timestamp = "2024-06-01 10:00:00", volume = 0f, flow = 0f)
        )
        coEvery { firebaseRepo.getLatestSensorData() } returns flowOf(fakeData)
        val repo = SensorRepository(firebaseRepo)
        val result = repo.getSensorStatusesFlow()
        val statuses = result.first()
        assertEquals(4, statuses.size)
        assertEquals("pH", statuses[0].name)
    }

    @Test
    fun fallbackLogicReturnsDummyDataWhenNoRealData() = runTest {
        val repo = SensorRepository(mockk(relaxed = true))
        val readings = repo.getSensorReadings()
        assertEquals(4, readings.size)
        assertEquals("0", readings[0].value) // TDS
        assertEquals("0", readings[1].value) // pH
        assertEquals("0", readings[2].value) // Turbidity
        assertEquals("N/A", readings[3].value) // Temperature
    }
} 