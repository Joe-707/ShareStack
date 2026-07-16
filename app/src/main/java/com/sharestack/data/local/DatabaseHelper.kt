package com.sharestack.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sharestack.models.Proposal
import com.sharestack.models.Stack
import com.sharestack.models.StackMember
import com.sharestack.models.User

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "sharestack.db"
        private const val DATABASE_VERSION = 2

        // Users Table
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_NAME = "name"

        // Stacks Table
        private const val TABLE_STACKS = "stacks"
        private const val COLUMN_STACK_ID = "stack_id"
        private const val COLUMN_STACK_NAME = "stack_name"
        private const val COLUMN_STOCK_SYMBOL = "stock_symbol"
        private const val COLUMN_SHARES_OWNED = "shares_owned"
        private const val COLUMN_PURCHASE_PRICE = "purchase_price"
        private const val COLUMN_CREATED_BY = "created_by"

        // Stack Members Table
        private const val TABLE_STACK_MEMBERS = "stack_members"
        private const val COLUMN_STACK_ID_REF = "stack_id"
        private const val COLUMN_MEMBER_NAME = "member_name"
        private const val COLUMN_OWNERSHIP_PERCENT = "ownership_percent"

        // Proposals Table
        private const val TABLE_PROPOSALS = "proposals"
        private const val COLUMN_PROPOSAL_ID = "proposal_id"
        private const val COLUMN_STACK_ID_REF_PROP = "stack_id"
        private const val COLUMN_STOCK_TARGET = "stock_target"
        private const val COLUMN_TARGET_AMOUNT = "target_amount"

        // Proposal Members Table
        private const val TABLE_PROPOSAL_MEMBERS = "proposal_members"
        private const val COLUMN_PROPOSAL_ID_REF = "proposal_id"
        private const val COLUMN_MEMBER_NAME_REF = "member_name"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users table
        db.execSQL("""
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_USERNAME TEXT NOT NULL UNIQUE,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_NAME TEXT NOT NULL
            )
        """.trimIndent())

        // Stacks table
        db.execSQL("""
            CREATE TABLE $TABLE_STACKS (
                $COLUMN_STACK_ID TEXT PRIMARY KEY,
                $COLUMN_STACK_NAME TEXT NOT NULL,
                $COLUMN_STOCK_SYMBOL TEXT NOT NULL,
                $COLUMN_SHARES_OWNED REAL DEFAULT 0,
                $COLUMN_PURCHASE_PRICE REAL DEFAULT 0,
                $COLUMN_CREATED_BY TEXT NOT NULL
            )
        """.trimIndent())

        // Stack Members table
        db.execSQL("""
            CREATE TABLE $TABLE_STACK_MEMBERS (
                $COLUMN_STACK_ID_REF TEXT NOT NULL,
                $COLUMN_MEMBER_NAME TEXT NOT NULL,
                $COLUMN_OWNERSHIP_PERCENT INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_STACK_ID_REF) REFERENCES $TABLE_STACKS($COLUMN_STACK_ID)
            )
        """.trimIndent())

        // Proposals table
        db.execSQL("""
            CREATE TABLE $TABLE_PROPOSALS (
                $COLUMN_PROPOSAL_ID TEXT PRIMARY KEY,
                $COLUMN_STACK_ID_REF_PROP TEXT NOT NULL,
                $COLUMN_STOCK_TARGET TEXT NOT NULL,
                $COLUMN_TARGET_AMOUNT REAL NOT NULL,
                FOREIGN KEY($COLUMN_STACK_ID_REF_PROP) REFERENCES $TABLE_STACKS($COLUMN_STACK_ID)
            )
        """.trimIndent())

        // Proposal Members table
        db.execSQL("""
            CREATE TABLE $TABLE_PROPOSAL_MEMBERS (
                $COLUMN_PROPOSAL_ID_REF TEXT NOT NULL,
                $COLUMN_MEMBER_NAME_REF TEXT NOT NULL,
                FOREIGN KEY($COLUMN_PROPOSAL_ID_REF) REFERENCES $TABLE_PROPOSALS($COLUMN_PROPOSAL_ID)
            )
        """.trimIndent())

        // Insert default data
        insertDefaultUser(db)
        insertDefaultStacks(db)
    }

    private fun insertDefaultUser(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COLUMN_ID, "u1")
            put(COLUMN_USERNAME, "demo@example.com")
            put(COLUMN_PASSWORD, "password")
            put(COLUMN_NAME, "Demo User")
        }
        db.insert(TABLE_USERS, null, values)
    }

    private fun insertDefaultStacks(db: SQLiteDatabase) {
        // Stack 1: Weekend Traders
        val stack1Id = "1"
        val values1 = ContentValues().apply {
            put(COLUMN_STACK_ID, stack1Id)
            put(COLUMN_STACK_NAME, "Weekend Traders")
            put(COLUMN_STOCK_SYMBOL, "NVDA")
            put(COLUMN_SHARES_OWNED, 1.0)
            put(COLUMN_PURCHASE_PRICE, 900.0)
            put(COLUMN_CREATED_BY, "u1")
        }
        db.insert(TABLE_STACKS, null, values1)

        // Stack 1 Members - using explicit ContentValues
        val memberNames1 = listOf("Joe", "Austin", "Sarah")
        val memberPercents1 = listOf(40, 35, 25)
        for (i in memberNames1.indices) {
            val memberValues = ContentValues().apply {
                put(COLUMN_STACK_ID_REF, stack1Id)
                put(COLUMN_MEMBER_NAME, memberNames1[i])
                put(COLUMN_OWNERSHIP_PERCENT, memberPercents1[i])
            }
            db.insert(TABLE_STACK_MEMBERS, null, memberValues)
        }

        // Proposal for Stack 1
        val proposal1Id = "p1"
        val proposalValues = ContentValues().apply {
            put(COLUMN_PROPOSAL_ID, proposal1Id)
            put(COLUMN_STACK_ID_REF_PROP, stack1Id)
            put(COLUMN_STOCK_TARGET, "Nvidia (NVDA)")
            put(COLUMN_TARGET_AMOUNT, 17000.0)
        }
        db.insert(TABLE_PROPOSALS, null, proposalValues)

        // Proposal Members
        listOf("Joe", "Austin", "Sarah").forEach { member ->
            val proposalMemberValues = ContentValues().apply {
                put(COLUMN_PROPOSAL_ID_REF, proposal1Id)
                put(COLUMN_MEMBER_NAME_REF, member)
            }
            db.insert(TABLE_PROPOSAL_MEMBERS, null, proposalMemberValues)
        }

        // Stack 2: Tech Bros
        val stack2Id = "2"
        val values2 = ContentValues().apply {
            put(COLUMN_STACK_ID, stack2Id)
            put(COLUMN_STACK_NAME, "Tech Bros")
            put(COLUMN_STOCK_SYMBOL, "AMZN")
            put(COLUMN_SHARES_OWNED, 0.5)
            put(COLUMN_PURCHASE_PRICE, 350.0)
            put(COLUMN_CREATED_BY, "u1")
        }
        db.insert(TABLE_STACKS, null, values2)

        val memberNames2 = listOf("Joe", "Mike")
        val memberPercents2 = listOf(50, 50)
        for (i in memberNames2.indices) {
            val memberValues = ContentValues().apply {
                put(COLUMN_STACK_ID_REF, stack2Id)
                put(COLUMN_MEMBER_NAME, memberNames2[i])
                put(COLUMN_OWNERSHIP_PERCENT, memberPercents2[i])
            }
            db.insert(TABLE_STACK_MEMBERS, null, memberValues)
        }

        // Stack 3: Value Hunters
        val stack3Id = "3"
        val values3 = ContentValues().apply {
            put(COLUMN_STACK_ID, stack3Id)
            put(COLUMN_STACK_NAME, "Value Hunters")
            put(COLUMN_STOCK_SYMBOL, "AAPL")
            put(COLUMN_SHARES_OWNED, 0.75)
            put(COLUMN_PURCHASE_PRICE, 150.0)
            put(COLUMN_CREATED_BY, "u1")
        }
        db.insert(TABLE_STACKS, null, values3)

        val memberNames3 = listOf("Joe", "Emma", "David")
        val memberPercents3 = listOf(30, 40, 30)
        for (i in memberNames3.indices) {
            val memberValues = ContentValues().apply {
                put(COLUMN_STACK_ID_REF, stack3Id)
                put(COLUMN_MEMBER_NAME, memberNames3[i])
                put(COLUMN_OWNERSHIP_PERCENT, memberPercents3[i])
            }
            db.insert(TABLE_STACK_MEMBERS, null, memberValues)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROPOSAL_MEMBERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROPOSALS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STACK_MEMBERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STACKS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ========== USER OPERATIONS ==========

    fun insertUser(id: String, username: String, password: String, name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_ID, id)
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password)
            put(COLUMN_NAME, name)
        }
        val result = db.insert(TABLE_USERS, null, values)
        db.close()
        return result != -1L
    }

    fun getUser(username: String, password: String): User? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_NAME, COLUMN_USERNAME),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, password),
            null, null, null
        )
        return if (cursor.moveToFirst()) {
            val id = cursor.getString(0)
            val name = cursor.getString(1)
            val email = cursor.getString(2)
            cursor.close()
            db.close()
            User(id, name, 0.0, email)
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun userExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    // ========== STACK OPERATIONS ==========

    fun insertStack(stack: Stack): Boolean {
        val db = writableDatabase
        try {
            db.beginTransaction()

            val stackValues = ContentValues().apply {
                put(COLUMN_STACK_ID, stack.id)
                put(COLUMN_STACK_NAME, stack.name)
                put(COLUMN_STOCK_SYMBOL, stack.stockSymbol)
                put(COLUMN_SHARES_OWNED, stack.sharesOwned)
                put(COLUMN_PURCHASE_PRICE, stack.purchasePrice)
                put(COLUMN_CREATED_BY, "u1")
            }
            db.insert(TABLE_STACKS, null, stackValues)

            stack.members.forEach { member ->
                val memberValues = ContentValues().apply {
                    put(COLUMN_STACK_ID_REF, stack.id)
                    put(COLUMN_MEMBER_NAME, member.name)
                    put(COLUMN_OWNERSHIP_PERCENT, member.ownershipPercentage)
                }
                db.insert(TABLE_STACK_MEMBERS, null, memberValues)
            }

            stack.activeProposals.forEach { proposal ->
                val proposalValues = ContentValues().apply {
                    put(COLUMN_PROPOSAL_ID, proposal.id)
                    put(COLUMN_STACK_ID_REF_PROP, stack.id)
                    put(COLUMN_STOCK_TARGET, proposal.stockTarget)
                    put(COLUMN_TARGET_AMOUNT, proposal.targetAmount)
                }
                db.insert(TABLE_PROPOSALS, null, proposalValues)

                proposal.activeMembers.forEach { member ->
                    val proposalMemberValues = ContentValues().apply {
                        put(COLUMN_PROPOSAL_ID_REF, proposal.id)
                        put(COLUMN_MEMBER_NAME_REF, member)
                    }
                    db.insert(TABLE_PROPOSAL_MEMBERS, null, proposalMemberValues)
                }
            }

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getAllStacks(): List<Stack> {
        val stacks = mutableListOf<Stack>()
        val db = readableDatabase

        val cursor = db.query(TABLE_STACKS, null, null, null, null, null, null)
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STACK_ID))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STACK_NAME))
            val stockSymbol = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STOCK_SYMBOL))
            val sharesOwned = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_SHARES_OWNED))
            val purchasePrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_PURCHASE_PRICE))

            val members = getStackMembers(id)
            val proposals = getProposalsForStack(id)

            stacks.add(
                Stack(
                    id = id,
                    name = name,
                    members = members,
                    stockSymbol = stockSymbol,
                    sharesOwned = sharesOwned,
                    purchasePrice = purchasePrice,
                    activeProposals = proposals
                )
            )
        }
        cursor.close()
        db.close()
        return stacks
    }

    private fun getStackMembers(stackId: String): List<StackMember> {
        val members = mutableListOf<StackMember>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_STACK_MEMBERS,
            null,
            "$COLUMN_STACK_ID_REF = ?",
            arrayOf(stackId),
            null, null, null
        )
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMBER_NAME))
            val percent = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_OWNERSHIP_PERCENT))
            members.add(StackMember(name, percent))
        }
        cursor.close()
        db.close()
        return members
    }

    private fun getProposalsForStack(stackId: String): List<Proposal> {
        val proposals = mutableListOf<Proposal>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROPOSALS,
            null,
            "$COLUMN_STACK_ID_REF_PROP = ?",
            arrayOf(stackId),
            null, null, null
        )
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PROPOSAL_ID))
            val stockTarget = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STOCK_TARGET))
            val targetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TARGET_AMOUNT))

            val members = getProposalMembers(id)
            proposals.add(Proposal(id, stockTarget, targetAmount, members))
        }
        cursor.close()
        db.close()
        return proposals
    }

    private fun getProposalMembers(proposalId: String): List<String> {
        val members = mutableListOf<String>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_PROPOSAL_MEMBERS,
            null,
            "$COLUMN_PROPOSAL_ID_REF = ?",
            arrayOf(proposalId),
            null, null, null
        )
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MEMBER_NAME_REF))
            members.add(name)
        }
        cursor.close()
        db.close()
        return members
    }

    fun insertProposal(proposal: Proposal, stackId: String): Boolean {
        val db = writableDatabase
        try {
            db.beginTransaction()

            val proposalValues = ContentValues().apply {
                put(COLUMN_PROPOSAL_ID, proposal.id)
                put(COLUMN_STACK_ID_REF_PROP, stackId)
                put(COLUMN_STOCK_TARGET, proposal.stockTarget)
                put(COLUMN_TARGET_AMOUNT, proposal.targetAmount)
            }
            db.insert(TABLE_PROPOSALS, null, proposalValues)

            proposal.activeMembers.forEach { member ->
                val memberValues = ContentValues().apply {
                    put(COLUMN_PROPOSAL_ID_REF, proposal.id)
                    put(COLUMN_MEMBER_NAME_REF, member)
                }
                db.insert(TABLE_PROPOSAL_MEMBERS, null, memberValues)
            }

            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    fun getStackById(stackId: String): Stack? {
        return getAllStacks().find { it.id == stackId }
    }
}