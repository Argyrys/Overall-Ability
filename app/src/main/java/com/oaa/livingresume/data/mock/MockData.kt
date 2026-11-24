package com.oaa.livingresume.data.mock

import com.oaa.livingresume.data.model.Milestone
import com.oaa.livingresume.data.model.Pillar
import com.oaa.livingresume.data.model.ProofStatus
import com.oaa.livingresume.data.model.ProofSubmission
import com.oaa.livingresume.data.model.SemesterEntry
import com.oaa.livingresume.data.model.SemesterStatus
import com.oaa.livingresume.data.model.SkillBadge
import com.oaa.livingresume.data.model.Student
import com.oaa.livingresume.data.model.TargetRole

/**
 * Realistic Indian-college mock data so the app compiles and runs immediately,
 * without any backend configured. B.Tech CSE "Batch of 2027".
 */
object MockData {

    const val MY_ID = "stu_21bce1042"
    const val COLLEGE = "Vidyaanand Institute of Technology, Pune"

    // ------------------------------------------------------------ the student
    // (lazy so it can reference [semesters], which is initialised below)

    val me: Student by lazy { Student(
        id = MY_ID,
        name = "Aarav Sharma",
        rollNo = "21BCE1042",
        branch = "B.Tech Computer Science & Engineering",
        college = COLLEGE,
        gradYear = 2027,
        targetRole = TargetRole.SOFTWARE_ENGINEER,
        email = "aarav.sharma@vit.edu",
        slug = "aarav-sharma-cse-2027",
        cgpa = 8.2,
        pillarScores = mapOf(
            Pillar.ACADEMIC to 84.0,
            Pillar.ADAPTABILITY to 76.0,
            Pillar.SOCIAL to 62.0,
            Pillar.PHYSICAL to 71.0,
        ),
        badges = listOf(
            SkillBadge("Top 5% Coder", "Campus-wide LeetCode rank", Pillar.ADAPTABILITY),
            SkillBadge("SIH 2025 Finalist", "Smart India Hackathon internal round", Pillar.ADAPTABILITY),
            SkillBadge("NPTEL Elite", "Data Structures & Algorithms", Pillar.ACADEMIC),
            SkillBadge("Top 10% CGPA", "Department ranking 2025-26", Pillar.ACADEMIC),
            SkillBadge("Club Treasurer", "Coding Club, 2025-26 term", Pillar.SOCIAL),
            SkillBadge("Perfect Attendance", "2 semesters in a row", Pillar.PHYSICAL),
        ),
        semesters = semesters,
        optedInPublic = true,
        headline = "Backend-leaning SWE · DSA + Spring Boot · hackathon finisher",
        skills = listOf("Kotlin", "Java", "Spring Boot", "Python", "SQL", "Git", "Docker", "REST APIs"),
        github = "github.com/aaravsharma-dev",
        linkedin = "linkedin.com/in/aaravsharma-cse",
    ) }

    // ------------------------------------------------------- 4-year timeline

    val semesters: List<SemesterEntry> = listOf(
        SemesterEntry(
            semester = 1,
            term = "Jul - Dec 2023",
            gpa = 7.4,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 70.0,
                Pillar.ADAPTABILITY to 52.0,
                Pillar.SOCIAL to 45.0,
                Pillar.PHYSICAL to 65.0,
            ),
            milestones = listOf(
                Milestone("Joined Coding Club", "Core member - first C++ & DSA workshop attended", Pillar.SOCIAL, true, "Aug 2023"),
                Milestone("Semester 1 register submission", "All 6 subjects cleared in first attempt", Pillar.PHYSICAL, true, "Dec 2023"),
            ),
            status = SemesterStatus.COMPLETED,
        ),
        SemesterEntry(
            semester = 2,
            term = "Jan - May 2024",
            gpa = 7.7,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 73.0,
                Pillar.ADAPTABILITY to 57.0,
                Pillar.SOCIAL to 50.0,
                Pillar.PHYSICAL to 64.0,
            ),
            milestones = listOf(
                Milestone("NPTEL Elite - Data Structures & Algorithms", "8-week NPTEL course, Elite percentile certificate", Pillar.ACADEMIC, true, "Apr 2024"),
            ),
            status = SemesterStatus.COMPLETED,
        ),
        SemesterEntry(
            semester = 3,
            term = "Jul - Dec 2024",
            gpa = 8.0,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 75.0,
                Pillar.ADAPTABILITY to 62.0,
                Pillar.SOCIAL to 52.0,
                Pillar.PHYSICAL to 67.0,
            ),
            milestones = listOf(
                Milestone("2nd Place - Vertex Hackathon '24", "48-hour build: expense-splitting PWA, team of 4", Pillar.ADAPTABILITY, true, "Oct 2024"),
            ),
            status = SemesterStatus.COMPLETED,
        ),
        SemesterEntry(
            semester = 4,
            term = "Jan - May 2025",
            gpa = 8.2,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 78.0,
                Pillar.ADAPTABILITY to 66.0,
                Pillar.SOCIAL to 54.0,
                Pillar.PHYSICAL to 66.0,
            ),
            milestones = listOf(
                Milestone("First open-source PR merged", "Bug fix merged into a React component library (1.2k stars)", Pillar.ADAPTABILITY, true, "Mar 2025"),
            ),
            status = SemesterStatus.COMPLETED,
        ),
        SemesterEntry(
            semester = 5,
            term = "Jul - Dec 2025",
            gpa = 8.4,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 80.0,
                Pillar.ADAPTABILITY to 70.0,
                Pillar.SOCIAL to 56.0,
                Pillar.PHYSICAL to 68.0,
            ),
            milestones = listOf(
                Milestone("Treasurer - Coding Club", "Elected by 120-member club; owns budget + sponsorship", Pillar.SOCIAL, true, "Aug 2025"),
                Milestone("SIH 2025 Finalist", "Smart India Hackathon internal round - top 6 teams", Pillar.ADAPTABILITY, true, "Sep 2025"),
            ),
            status = SemesterStatus.COMPLETED,
        ),
        SemesterEntry(
            semester = 6,
            term = "Jan - May 2026",
            gpa = 8.6,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 82.0,
                Pillar.ADAPTABILITY to 73.0,
                Pillar.SOCIAL to 59.0,
                Pillar.PHYSICAL to 70.0,
            ),
            milestones = listOf(
                Milestone("Backend Internship - Zenlayer Systems", "4 weeks, stipend INR 25k; shipped 2 internal REST services", Pillar.ADAPTABILITY, true, "May 2026"),
                Milestone("Top 5% Coder", "Campus LeetCode rank 41 / 900+ active coders", Pillar.ADAPTABILITY, true, "May 2026"),
            ),
            status = SemesterStatus.COMPLETED,
        ),
        SemesterEntry(
            semester = 7,
            term = "Jul - Dec 2026",
            gpa = 8.7,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 84.0,
                Pillar.ADAPTABILITY to 76.0,
                Pillar.SOCIAL to 62.0,
                Pillar.PHYSICAL to 71.0,
            ),
            milestones = listOf(
                Milestone("Mentor for 4 juniors", "Weekly DSA doubt sessions, 6 weeks, club verified", Pillar.SOCIAL, true, "Sep 2026"),
                Milestone("92% attendance streak", "All subjects above 90% through mid-semester", Pillar.PHYSICAL, true, "Oct 2026"),
            ),
            status = SemesterStatus.ONGOING,
        ),
        SemesterEntry(
            semester = 8,
            term = "Jan - May 2027",
            gpa = 0.0,
            pillarScores = emptyMap(),
            milestones = emptyList(),
            status = SemesterStatus.UPCOMING,
            note = "Final semester: capstone project + placement season. Target: A in Adaptability & Social.",
        ),
    )

    // -------------------------------------------------------- batch mates

    val batchmates: List<Student> = listOf(
        Student(
            id = "stu_21bce1188", name = "Ishita Reddy", rollNo = "21BCE1188",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.DATA_ANALYST, email = "ishita.reddy@vit.edu",
            slug = "ishita-reddy-cse-2027", cgpa = 8.9,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 91.0, Pillar.ADAPTABILITY to 72.0,
                Pillar.SOCIAL to 68.0, Pillar.PHYSICAL to 80.0,
            ),
            optedInPublic = true, headline = "Analytics · SQL + Power BI",
        ),
        Student(
            id = "stu_21bce1207", name = "Karthik Nair", rollNo = "21BCE1207",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.ML_ENGINEER, email = "karthik.nair@vit.edu",
            slug = "karthik-nair-cse-2027", cgpa = 8.4,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 78.0, Pillar.ADAPTABILITY to 88.0,
                Pillar.SOCIAL to 55.0, Pillar.PHYSICAL to 66.0,
            ),
            optedInPublic = true, headline = "ML · PyTorch + Kaggle",
        ),
        Student(
            id = "stu_21bce1263", name = "Priya Deshmukh", rollNo = "21BCE1263",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.PRODUCT_MANAGER, email = "priya.d@vit.edu",
            slug = "priya-deshmukh-cse-2027", cgpa = 8.7,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 82.0, Pillar.ADAPTABILITY to 70.0,
                Pillar.SOCIAL to 90.0, Pillar.PHYSICAL to 74.0,
            ),
            optedInPublic = true, headline = "PM track · Rotaract chairperson",
        ),
        Student(
            id = "stu_21bce1319", name = "Rohan Gupta", rollNo = "21BCE1319",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.SOFTWARE_ENGINEER, email = "rohan.gupta@vit.edu",
            slug = "rohan-gupta-cse-2027", cgpa = 7.9,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 69.0, Pillar.ADAPTABILITY to 92.0,
                Pillar.SOCIAL to 58.0, Pillar.PHYSICAL to 61.0,
            ),
            optedInPublic = true, headline = "DSA monster · Codeforces Specialist",
        ),
        Student(
            id = "stu_21bce1355", name = "Ananya Iyer", rollNo = "21BCE1355",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.DATA_ANALYST, email = "ananya.iyer@vit.edu",
            slug = "ananya-iyer-cse-2027", cgpa = 8.8,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 88.0, Pillar.ADAPTABILITY to 64.0,
                Pillar.SOCIAL to 77.0, Pillar.PHYSICAL to 85.0,
            ),
            optedInPublic = true, headline = "Data viz · Tableau campus champion",
        ),
        Student(
            id = "stu_21bce1402", name = "Vishal Krishnan", rollNo = "21BCE1402",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.SOFTWARE_ENGINEER, email = "vishal.k@vit.edu",
            slug = "vishal-krishnan-cse-2027", cgpa = 8.1,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 74.0, Pillar.ADAPTABILITY to 81.0,
                Pillar.SOCIAL to 70.0, Pillar.PHYSICAL to 58.0,
            ),
            optedInPublic = true, headline = "Full-stack · React + Node",
        ),
        Student(
            id = "stu_21bce1478", name = "Sneha Patel", rollNo = "21BCE1478",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.ML_ENGINEER, email = "sneha.patel@vit.edu",
            slug = "sneha-patel-cse-2027", cgpa = 9.2,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 94.0, Pillar.ADAPTABILITY to 76.0,
                Pillar.SOCIAL to 72.0, Pillar.PHYSICAL to 69.0,
            ),
            optedInPublic = true, headline = "NLP · college topper",
        ),
        Student(
            id = "stu_21bce1521", name = "Arjun Menon", rollNo = "21BCE1521",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.PRODUCT_MANAGER, email = "arjun.menon@vit.edu",
            slug = "arjun-menon-cse-2027", cgpa = 7.8,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 65.0, Pillar.ADAPTABILITY to 71.0,
                Pillar.SOCIAL to 86.0, Pillar.PHYSICAL to 79.0,
            ),
            optedInPublic = true, headline = "TechFest event head · public speaker",
        ),
        Student(
            id = "stu_21bce1604", name = "Tanvi Kulkarni", rollNo = "21BCE1604",
            branch = "B.Tech CSE", college = COLLEGE, gradYear = 2027,
            targetRole = TargetRole.SOFTWARE_ENGINEER, email = "tanvi.k@vit.edu",
            slug = "tanvi-kulkarni-cse-2027", cgpa = 8.6,
            pillarScores = mapOf(
                Pillar.ACADEMIC to 86.0, Pillar.ADAPTABILITY to 74.0,
                Pillar.SOCIAL to 64.0, Pillar.PHYSICAL to 92.0,
            ),
            optedInPublic = false, headline = "Hidden from batch - prefers privacy",
        ),
    )

    /** Everyone including me, for leaderboard ranking. */
    val allStudents: List<Student> = listOf(me) + batchmates

    // ----------------------------------------------------- proof queue

    val submissions: List<ProofSubmission> = listOf(
        ProofSubmission(
            id = "sub_1001", studentId = MY_ID, studentName = "Aarav Sharma",
            title = "2nd Place - VertexHack '26 intra-college hackathon",
            link = "https://devfolio.co/projects/vertexhack-26/9f21",
            pillar = Pillar.ADAPTABILITY, points = 6, status = ProofStatus.PENDING,
            submittedAt = "12 Nov 2026", attachment = "vertexhack26_certificate.pdf",
        ),
        ProofSubmission(
            id = "sub_1002", studentId = MY_ID, studentName = "Aarav Sharma",
            title = "Lead Organizer - CodeStorm'26 workshop (60+ attendees)",
            link = "https://codingclub.vit.edu/codestorm-26",
            pillar = Pillar.SOCIAL, points = 5, status = ProofStatus.PENDING,
            submittedAt = "10 Nov 2026", attachment = "codestorm_attendance.jpg",
        ),
        ProofSubmission(
            id = "sub_1003", studentId = "stu_21bce1319", studentName = "Rohan Gupta",
            title = "LeetCode Knight Badge - 500+ problems solved",
            link = "https://leetcode.com/u/rohan_g_cse/",
            pillar = Pillar.ADAPTABILITY, points = 6, status = ProofStatus.PENDING,
            submittedAt = "11 Nov 2026",
        ),
        ProofSubmission(
            id = "sub_1004", studentId = "stu_21bce1263", studentName = "Priya Deshmukh",
            title = "Rotaract Club - Chairperson appointment letter",
            link = "https://rotaract.vit.edu/2026-appointments",
            pillar = Pillar.SOCIAL, points = 7, status = ProofStatus.PENDING,
            submittedAt = "09 Nov 2026", attachment = "rotaract_letter.pdf",
        ),
        ProofSubmission(
            id = "sub_1005", studentId = "stu_21bce1402", studentName = "Vishal Krishnan",
            title = "Smart India Hackathon 2026 - Zonal Winner",
            link = "https://sih.gov.in/2026/zonal/pune-04",
            pillar = Pillar.ADAPTABILITY, points = 9, status = ProofStatus.PENDING,
            submittedAt = "08 Nov 2026", attachment = "sih_zonal_winner.png",
        ),
        ProofSubmission(
            id = "sub_1006", studentId = "stu_21bce1604", studentName = "Tanvi Kulkarni",
            title = "NPTEL - Database Management Systems (Elite)",
            link = "https://swayam.gov.in/courses/nptel-235cs08",
            pillar = Pillar.ACADEMIC, points = 6, status = ProofStatus.PENDING,
            submittedAt = "07 Nov 2026", attachment = "nptel_dbs_elite.pdf",
        ),
        ProofSubmission(
            id = "sub_1007", studentId = "stu_21bce1521", studentName = "Arjun Menon",
            title = "TechFest'26 - Event Head (500+ footfall)",
            link = "https://techfest.vit.edu/2026/ops",
            pillar = Pillar.SOCIAL, points = 8, status = ProofStatus.PENDING,
            submittedAt = "06 Nov 2026", attachment = "techfest_report.pdf",
        ),
        ProofSubmission(
            id = "sub_1008", studentId = "stu_21bce1188", studentName = "Ishita Reddy",
            title = "Google Data Analytics Professional Certificate",
            link = "https://coursera.org/verify/professional-cert/4X8KD9",
            pillar = Pillar.ACADEMIC, points = 8, status = ProofStatus.APPROVED,
            submittedAt = "02 Nov 2026", attachment = "google_da_cert.pdf",
            verifierNote = "Verified on Coursera - approved by Dr. Meena Iyer (Prof-in-charge, CSE).",
        ),
        ProofSubmission(
            id = "sub_1009", studentId = "stu_21bce1402", studentName = "Vishal Krishnan",
            title = "Hackathon participation - blurry group photo",
            link = "https://imgur.com/a/unverified",
            pillar = Pillar.ADAPTABILITY, points = 8, status = ProofStatus.REJECTED,
            submittedAt = "30 Oct 2026",
            verifierNote = "Link does not resolve and the certificate name is not visible. Please re-upload the original certificate.",
        ),
    )
}
