package com.example.universalyoga
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
class YogaDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "yoga.db"
        private const val DATABASE_VERSION = 1

        // Courses table
        private const val TABLE_COURSES = "courses"
        private const val COLUMN_COURSE_ID = "id"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_DAY = "day_of_week"
        private const val COLUMN_TIME = "time_of_day"
        private const val COLUMN_DURATION = "duration"
        private const val COLUMN_CAPACITY = "capacity"
        private const val COLUMN_PRICE = "price"
        private const val COLUMN_DESCRIPTION = "description"

        // Classes table
        private const val TABLE_CLASSES = "classes"
        private const val COLUMN_CLASS_ID = "id"
        private const val COLUMN_COURSE_ID_FK = "course_id"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_CLASS_TEACHER = "teacher"
        private const val COLUMN_COMMENTS = "comments"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Create courses table
        val createCoursesTable = """
            CREATE TABLE $TABLE_COURSES (
                $COLUMN_COURSE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_DAY TEXT NOT NULL,
                $COLUMN_TIME TEXT NOT NULL,
                $COLUMN_DURATION INTEGER NOT NULL,
                $COLUMN_CAPACITY INTEGER NOT NULL,
                $COLUMN_PRICE REAL NOT NULL,
                $COLUMN_DESCRIPTION TEXT
            )
        """.trimIndent()

        // Create classes table with foreign key
        val createClassesTable = """
            CREATE TABLE $TABLE_CLASSES (
                $COLUMN_CLASS_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_COURSE_ID_FK INTEGER NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_CLASS_TEACHER TEXT NOT NULL,
                $COLUMN_COMMENTS TEXT,
                FOREIGN KEY ($COLUMN_COURSE_ID_FK) 
                    REFERENCES $TABLE_COURSES($COLUMN_COURSE_ID)
                    ON DELETE CASCADE
            )
        """.trimIndent()
        try {
            db?.execSQL(createCoursesTable)
            db?.execSQL(createClassesTable)
            Log.d("DB", "Tables created successfully")
        } catch (e: Exception) {
            Log.e("DB", "Error creating tables: ${e.message}")
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        try {
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_CLASSES")
            db?.execSQL("DROP TABLE IF EXISTS $TABLE_COURSES")
            onCreate(db)
            Log.d("DB", "Tables upgraded successfully")
        } catch (e: Exception) {
            Log.e("DB", "Error upgrading tables: ${e.message}")
        }
    }
    override fun onConfigure(db: SQLiteDatabase?) {
        super.onConfigure(db)
        // Enable foreign keys
        db?.setForeignKeyConstraintsEnabled(true)
    }
    // Course CRUD operations
    fun insertCourse(course: YogaCourse): Long {
        val values = ContentValues().apply {
            put(COLUMN_TYPE, course.type)
            put(COLUMN_DAY, course.dayOfWeek)
            put(COLUMN_TIME, course.timeOfDay)
            put(COLUMN_DURATION, course.duration)
            put(COLUMN_CAPACITY, course.capacity)
            put(COLUMN_PRICE, course.price)
            put(COLUMN_DESCRIPTION, course.description)
        }
        return writableDatabase.insert(TABLE_COURSES, null, values)
    }

    fun updateCourse(id: Long, course: YogaCourse): Int {
        val values = ContentValues().apply {
            put(COLUMN_TYPE, course.type)
            put(COLUMN_DAY, course.dayOfWeek)
            put(COLUMN_TIME, course.timeOfDay)
            put(COLUMN_DURATION, course.duration)
            put(COLUMN_CAPACITY, course.capacity)
            put(COLUMN_PRICE, course.price)
            put(COLUMN_DESCRIPTION, course.description)
        }
        return writableDatabase.update(
            TABLE_COURSES,
            values,
            "$COLUMN_COURSE_ID = ?",
            arrayOf(id.toString())
        )
    }

    fun deleteCourse(id: Long): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_COURSES, "$COLUMN_COURSE_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun getAllCourses(): List<YogaCourse> {
        val courses = mutableListOf<YogaCourse>()
        val query = "SELECT * FROM $TABLE_COURSES"
        val cursor = readableDatabase.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                courses.add(
                    YogaCourse(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                        type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                        dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY)),
                        timeOfDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                        duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                        capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                        price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return courses
    }

    // Class CRUD operations
    fun insertClass(yogaClass: YogaClass): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COURSE_ID_FK, yogaClass.courseId)
            put(COLUMN_DATE, yogaClass.date)
            put(COLUMN_CLASS_TEACHER, yogaClass.teacher)
            put(COLUMN_COMMENTS, yogaClass.comments)
        }
        val id = db.insert(TABLE_CLASSES, null, values)
        db.close()
        return id
    }

    fun updateClass(id: Long, yogaClass: YogaClass): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_COURSE_ID_FK, yogaClass.courseId)
            put(COLUMN_DATE, yogaClass.date)
            put(COLUMN_CLASS_TEACHER, yogaClass.teacher)
            put(COLUMN_COMMENTS, yogaClass.comments)
        }
        val result = db.update(TABLE_CLASSES, values, "$COLUMN_CLASS_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }

    fun deleteClass(id: Long): Int {
        val db = writableDatabase
        val result = db.delete(TABLE_CLASSES, "$COLUMN_CLASS_ID = ?", arrayOf(id.toString()))
        db.close()
        return result
    }
    fun deleteClassesForCourse(courseId: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_CLASSES, "$COLUMN_COURSE_ID_FK = ?", arrayOf(courseId.toString()))
    }
    fun getClassesForCourse(courseId: Long): List<YogaClass> {
        val classes = mutableListOf<YogaClass>()
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_CLASSES WHERE $COLUMN_COURSE_ID_FK = ?"
        val cursor = db.rawQuery(query, arrayOf(courseId.toString()))

        if (cursor.moveToFirst()) {
            do {
                classes.add(
                    YogaClass(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)),
                        courseId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID_FK)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                        teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_TEACHER)),
                        comments = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classes
    }
    fun getAllClasses(): List<YogaClass> {
        val classes = mutableListOf<YogaClass>()
        val db = this.readableDatabase
        val query = "SELECT * FROM $TABLE_CLASSES"

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val yogaClass = YogaClass(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CLASS_ID)),
                    courseId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                    date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                    teacher = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_TEACHER)),
                    comments = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENTS))
                )
                classes.add(yogaClass)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return classes
    }

    fun searchClassesByTeacher(teacherName: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val db = readableDatabase

        // Modified query to use LOWER() for case-insensitive search
        val query = """
        SELECT 
            c.$COLUMN_CLASS_ID as classId,
            c.$COLUMN_DATE as classDate,
            c.$COLUMN_CLASS_TEACHER as classTeacher,
            co.$COLUMN_COURSE_ID as courseId,
            co.$COLUMN_TYPE as courseType,
            co.$COLUMN_TIME as courseTime,
            co.$COLUMN_DAY as courseDay
        FROM $TABLE_CLASSES c
        INNER JOIN $TABLE_COURSES co ON c.$COLUMN_COURSE_ID_FK = co.$COLUMN_COURSE_ID
        WHERE LOWER(c.$COLUMN_CLASS_TEACHER) LIKE LOWER(?)
        ORDER BY c.$COLUMN_DATE ASC
    """

        val searchPattern = "%${teacherName.trim()}%"
        val cursor = db.rawQuery(query, arrayOf(searchPattern))

        if (cursor.moveToFirst()) {
            do {
                results.add(
                    SearchResult(
                        classId = cursor.getLong(cursor.getColumnIndexOrThrow("classId")),
                        courseId = cursor.getLong(cursor.getColumnIndexOrThrow("courseId")),
                        courseType = cursor.getString(cursor.getColumnIndexOrThrow("courseType")),
                        classDate = cursor.getString(cursor.getColumnIndexOrThrow("classDate")),
                        teacherName = cursor.getString(cursor.getColumnIndexOrThrow("classTeacher")),
                        courseTime = cursor.getString(cursor.getColumnIndexOrThrow("courseTime")),
                        courseDay = cursor.getString(cursor.getColumnIndexOrThrow("courseDay"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }

    fun searchClassesByDate(date: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val db = readableDatabase

        // We need an exact match for the date
        val query = """
        SELECT 
            c.$COLUMN_CLASS_ID as classId,
            c.$COLUMN_DATE as classDate,
            c.$COLUMN_CLASS_TEACHER as classTeacher,
            co.$COLUMN_COURSE_ID as courseId,
            co.$COLUMN_TYPE as courseType,
            co.$COLUMN_TIME as courseTime,
            co.$COLUMN_DAY as courseDay
        FROM $TABLE_CLASSES c
        INNER JOIN $TABLE_COURSES co ON c.$COLUMN_COURSE_ID_FK = co.$COLUMN_COURSE_ID
        WHERE c.$COLUMN_DATE = ?
        ORDER BY co.$COLUMN_TIME ASC
        """

        val cursor = db.rawQuery(query, arrayOf(date))

        if (cursor.moveToFirst()) {
            do {
                results.add(
                    SearchResult(
                        classId = cursor.getLong(cursor.getColumnIndexOrThrow("classId")),
                        courseId = cursor.getLong(cursor.getColumnIndexOrThrow("courseId")),
                        courseType = cursor.getString(cursor.getColumnIndexOrThrow("courseType")),
                        classDate = cursor.getString(cursor.getColumnIndexOrThrow("classDate")),
                        teacherName = cursor.getString(cursor.getColumnIndexOrThrow("classTeacher")),
                        courseTime = cursor.getString(cursor.getColumnIndexOrThrow("courseTime")),
                        courseDay = cursor.getString(cursor.getColumnIndexOrThrow("courseDay"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }

    fun searchClassesByDayOfWeek(dayOfWeek: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val db = readableDatabase

        val query = """
        SELECT DISTINCT
            c.$COLUMN_CLASS_ID as classId,
            c.$COLUMN_DATE as classDate,
            c.$COLUMN_CLASS_TEACHER as classTeacher,
            co.$COLUMN_COURSE_ID as courseId,
            co.$COLUMN_TYPE as courseType,
            co.$COLUMN_TIME as courseTime,
            co.$COLUMN_DAY as courseDay
        FROM $TABLE_CLASSES c
        INNER JOIN $TABLE_COURSES co ON c.$COLUMN_COURSE_ID_FK = co.$COLUMN_COURSE_ID
        WHERE co.$COLUMN_DAY = ?
        ORDER BY co.$COLUMN_TIME ASC, c.$COLUMN_DATE ASC
    """

        val cursor = db.rawQuery(query, arrayOf(dayOfWeek))

        if (cursor.moveToFirst()) {
            do {
                results.add(
                    SearchResult(
                        classId = cursor.getLong(cursor.getColumnIndexOrThrow("classId")),
                        courseId = cursor.getLong(cursor.getColumnIndexOrThrow("courseId")),
                        courseType = cursor.getString(cursor.getColumnIndexOrThrow("courseType")),
                        classDate = cursor.getString(cursor.getColumnIndexOrThrow("classDate")),
                        teacherName = cursor.getString(cursor.getColumnIndexOrThrow("classTeacher")),
                        courseTime = cursor.getString(cursor.getColumnIndexOrThrow("courseTime")),
                        courseDay = cursor.getString(cursor.getColumnIndexOrThrow("courseDay"))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return results
    }

    fun getCourseById(courseId: Long): YogaCourse? {
        val db = readableDatabase
        val query = "SELECT * FROM $TABLE_COURSES WHERE $COLUMN_COURSE_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(courseId.toString()))

        return if (cursor.moveToFirst()) {
            val course = YogaCourse(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_COURSE_ID)),
                type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
                dayOfWeek = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DAY)),
                timeOfDay = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIME)),
                duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION)),
                capacity = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CAPACITY)),
                price = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PRICE)),
                description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
            )
            cursor.close()
            course
        } else {
            cursor.close()
            null
        }
    }
}