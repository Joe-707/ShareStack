package com.sharestack.data.remote

import com.sharestack.models.Proposal
import com.sharestack.models.Stack
import com.sharestack.models.StackMember
import com.sharestack.models.User
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URLEncoder

class SupabaseService {

    private val supabaseUrl = "https://vsmnhnaqbhdjihsfcxvy.supabase.co/rest/v1/"
    private val supabaseKey = "sb_publishable_WeK-bYF1iEiF8qwrZPa7iQ_g7vpg6pi"

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    // ========== USER OPERATIONS ==========

    suspend fun getUser(username: String, password: String): User? {
        return try {
            println("🔍 Supabase: Looking for user with username=$username")

            // ✅ Try a simpler URL without encoding first
            val url = "$supabaseUrl/users?username=eq.$username&password=eq.$password"

            println("🔍 Supabase URL: $url")

            val response = client.get(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }
            val text = response.bodyAsText()
            println("🔍 Supabase Response: $text")

            if (!text.startsWith("[")) {
                println("🔍 Supabase returned error: $text")
                return null
            }

            val jsonArray = json.decodeFromString<JsonArray>(text)

            if (jsonArray.isEmpty()) {
                println("🔍 Supabase: No user found")
                return null
            }

            val obj = jsonArray[0].jsonObject
            User(
                id = obj["id"]?.jsonPrimitive?.content ?: "",
                name = obj["name"]?.jsonPrimitive?.content ?: "",
                totalPortfolioValue = 0.0,
                email = obj["username"]?.jsonPrimitive?.content ?: ""
            )
        } catch (e: Exception) {
            println("🔍 Supabase Error: ${e.message}")
            null
        }
    }

    suspend fun insertUser(user: User, password: String): Boolean {
        return try {
            val jsonString = """
            {"id":"${user.id}","username":"${user.email}","password":"$password","name":"${user.name}"}
        """.trimIndent()

            client.post("$supabaseUrl/users") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(jsonString)
            }
            true
        } catch (e: Exception) {
            println("Error inserting user: ${e.message}")
            false
        }
    }

    suspend fun userExists(username: String): Boolean {
        return try {
            val encodedUsername = URLEncoder.encode(username, "UTF-8")
            val url = "$supabaseUrl/users?username=eq.$encodedUsername"

            val response = client.get(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }
            val text = response.bodyAsText()
            if (!text.startsWith("[")) return false
            val jsonArray = json.decodeFromString<JsonArray>(text)
            jsonArray.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    // ========== STACK OPERATIONS ==========

    suspend fun getAllStacks(): List<Stack> {
        return try {
            val response = client.get("$supabaseUrl/stacks") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }
            val text = response.bodyAsText()
            if (!text.startsWith("[")) return emptyList()

            val jsonArray = json.decodeFromString<JsonArray>(text)

            jsonArray.map { element ->
                val obj = element.jsonObject
                val stackId = obj["stack_id"]?.jsonPrimitive?.content ?: ""

                Stack(
                    id = stackId,
                    name = obj["stack_name"]?.jsonPrimitive?.content ?: "",
                    members = getStackMembers(stackId),
                    stockSymbol = obj["stock_symbol"]?.jsonPrimitive?.content ?: "",
                    sharesOwned = obj["shares_owned"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                    purchasePrice = obj["purchase_price"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                    activeProposals = getProposalsForStack(stackId)
                )
            }
        } catch (e: Exception) {
            println("Error fetching stacks: ${e.message}")
            emptyList()
        }
    }

    suspend fun insertStack(stack: Stack): Boolean {
        return try {
            val jsonString = """
                {"stack_id":"${stack.id}","stack_name":"${stack.name}","stock_symbol":"${stack.stockSymbol}","shares_owned":${stack.sharesOwned},"purchase_price":${stack.purchasePrice},"created_by":"u1"}
            """.trimIndent()

            client.post("$supabaseUrl/stacks") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(jsonString)
            }

            stack.members.forEach { member ->
                insertStackMember(stack.id, member.name, member.ownershipPercentage)
            }
            true
        } catch (e: Exception) {
            println("Error inserting stack: ${e.message}")
            false
        }
    }

    suspend fun getStackMembers(stackId: String): List<StackMember> {
        return try {
            val encodedStackId = URLEncoder.encode(stackId, "UTF-8")
            val url = "$supabaseUrl/stack_members?stack_id=eq.$encodedStackId"

            val response = client.get(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }
            val text = response.bodyAsText()
            if (!text.startsWith("[")) return emptyList()

            val jsonArray = json.decodeFromString<JsonArray>(text)

            jsonArray.map { element ->
                val obj = element.jsonObject
                StackMember(
                    name = obj["member_name"]?.jsonPrimitive?.content ?: "",
                    ownershipPercentage = obj["ownership_percent"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertStackMember(stackId: String, memberName: String, percent: Int): Boolean {
        return try {
            val jsonString = """
                {"stack_id":"$stackId","member_name":"$memberName","ownership_percent":$percent}
            """.trimIndent()

            client.post("$supabaseUrl/stack_members") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(jsonString)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // ========== PROPOSAL OPERATIONS ==========

    suspend fun getProposalsForStack(stackId: String): List<Proposal> {
        return try {
            val encodedStackId = URLEncoder.encode(stackId, "UTF-8")
            val url = "$supabaseUrl/proposals?stack_id=eq.$encodedStackId"

            val response = client.get(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }
            val text = response.bodyAsText()
            if (!text.startsWith("[")) return emptyList()

            val jsonArray = json.decodeFromString<JsonArray>(text)

            jsonArray.map { element ->
                val obj = element.jsonObject
                val proposalId = obj["proposal_id"]?.jsonPrimitive?.content ?: ""
                Proposal(
                    id = proposalId,
                    stockTarget = obj["stock_target"]?.jsonPrimitive?.content ?: "",
                    targetAmount = obj["target_amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0,
                    activeMembers = getProposalMembers(proposalId)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertProposal(proposal: Proposal, stackId: String): Boolean {
        return try {
            val jsonString = """
                {"proposal_id":"${proposal.id}","stack_id":"$stackId","stock_target":"${proposal.stockTarget}","target_amount":${proposal.targetAmount}}
            """.trimIndent()

            client.post("$supabaseUrl/proposals") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(jsonString)
            }

            proposal.activeMembers.forEach { member ->
                insertProposalMember(proposal.id, member)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getProposalMembers(proposalId: String): List<String> {
        return try {
            val encodedProposalId = URLEncoder.encode(proposalId, "UTF-8")
            val url = "$supabaseUrl/proposal_members?proposal_id=eq.$encodedProposalId"

            val response = client.get(url) {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
            }
            val text = response.bodyAsText()
            if (!text.startsWith("[")) return emptyList()

            val jsonArray = json.decodeFromString<JsonArray>(text)

            jsonArray.mapNotNull { element ->
                element.jsonObject["member_name"]?.jsonPrimitive?.content
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun insertProposalMember(proposalId: String, memberName: String): Boolean {
        return try {
            val jsonString = """
                {"proposal_id":"$proposalId","member_name":"$memberName"}
            """.trimIndent()

            client.post("$supabaseUrl/proposal_members") {
                header("apikey", supabaseKey)
                header("Authorization", "Bearer $supabaseKey")
                contentType(ContentType.Application.Json)
                setBody(jsonString)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
}