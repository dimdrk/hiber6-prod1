package gr.aueb.cf;


import gr.aueb.cf.model.Course;
import gr.aueb.cf.model.Teacher;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;

import java.util.List;

public class App
{
    public static void main( String[] args )
    {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("school1PU");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();

        // Select all teachers
        String sql = "SELECT t FROM Teacher t";
        TypedQuery<Teacher> query = em.createQuery(sql, Teacher.class);
        List<Teacher> teachers = query.getResultList();

        // Select all courses
        String sql2 = "SELECT c FROM Course c";
        TypedQuery<Course> query2 = em.createQuery(sql2, Course.class);
        List<Course> courses = query2.getResultList();

        // Select courses taught by Αθανάσιος
        String sql3 = "SELECT c FROM Course c WHERE c.teacher.firstname = :firstname";
        TypedQuery<Course> query3 = em.createQuery(sql3, Course.class);
        query3.setParameter("firstname", "Αθανάσιος");
        List<Course> courses3 = query3.getResultList();

        // Select teachers & course.titles
        String sql4 = "SELECT t, c.title FROM Teacher t JOIN t.courses c";
        TypedQuery<Object[]> query4 = em.createQuery(sql4, Object[].class);
        List<Object[]> teachersCourseTitles = query4.getResultList();

        // Select teachers that teach Java
        String sql5 = "SELECT t FROM Teacher t JOIN t.courses c WHERE c.title = :courseTitle";
        TypedQuery<Teacher> query5 = em.createQuery(sql5, Teacher.class);
        query5.setParameter("courseTitle", "Java");
        List<Teacher> teachersJava = query5.getResultList();

        // Select teacher.firstname, lastname, count of courses they teach
        String sql6 = "SELECT t, COUNT(c) FROM Teacher t JOIN t.courses c GROUP BY t";
        TypedQuery<Object[]> query6 = em.createQuery(sql6, Object[].class);
        List<Object[]> teachersCoursesCount = query6.getResultList();

        // Select teachers that teach more than one courses
        String sql7 = "SELECT t FROM Teacher t JOIN t.courses c GROUP BY t HAVING COUNT(c) > 1";
        TypedQuery<Teacher> query7 = em.createQuery(sql7, Teacher.class);
        List<Teacher> teachersMoreThanOneCourses = query7.getResultList();

        // Select teachers & courses they teach ordered by lastname, title
        String sql8 = "SELECT t, c FROM Teacher t JOIN t.courses c ORDER BY t.lastname ASC, c.title ASC";
        TypedQuery<Object[]> query8 = em.createQuery(sql8, Object[].class);
        List<Object[]> teachersCoursesOrdered = query8.getResultList();

        // Select teachers that do not teach a course
        String sql9 = "SELECT t FROM Teacher t LEFT JOIN t.courses c WHERE c IS NULL";
        TypedQuery<Teacher> query9 = em.createQuery(sql9, Teacher.class);
        List<Teacher> teachersWithNoCourses = query9.getResultList();

        // Select the most popular courses by teachers' count
        String sql10 = "SELECT c.title, COUNT(t) FROM Course c JOIN c.teacher t GROUP BY c.title ORDER BY COUNT(t) DESC";
        TypedQuery<Object[]> query10 = em.createQuery(sql10, Object[].class);
        List<Object[]> popularCourses = query10.getResultList();


        /**
         * Criteria API.
         *
         * Better than JPQL for dynamic queries.
         * Provides two main interfaces: 1) CriteriaBuilder, 2) CriteriaQuery<T>
         *
         * CriteriaBuilder provides API for creating queries, entity roots (FROM)
         * returned results, and defining predicates (boolean expressions in WHERE clauses).
         *
         * CriteriaQuery<T> provides API for creating queries, entity roots (FROM)
         * returned results, and for adding criteria (WHERE)
         *
         */

        // List courses
        CriteriaBuilder cb1 = em.getCriteriaBuilder();
        CriteriaQuery<String> query11 = cb1.createQuery(String.class);
        Root<Course> course = query11.from(Course.class);
        query11.select(course.get("title"));
        List<String> titles = em.createQuery(query11).getResultList();

        // Select teachers with a specific lastname
        CriteriaBuilder cb2 = em.getCriteriaBuilder();
        CriteriaQuery<Teacher> query12 = cb2.createQuery(Teacher.class);
        Root<Teacher> teacher = query12.from(Teacher.class);
        ParameterExpression<String> lastnameParam = cb2.parameter(String.class, "lastname");
        query12.select(teacher).where(cb2.equal(teacher.get("lastname"), lastnameParam));
        List<Teacher> teachersLastname = em.createQuery(query12).setParameter("lastname", "Ανδρούτσος").getResultList();

        // Find courses taught by a specific teacher
        CriteriaBuilder cb3 = em.getCriteriaBuilder();
        CriteriaQuery<Course> query13 = cb3.createQuery(Course.class);
        Root<Course> courseByTeacher = query13.from(Course.class);
        Join<Course, Teacher> teacherToJoin = courseByTeacher.join("teacher");

        ParameterExpression<String> lastnameParameter = cb3.parameter(String.class, "lastname");
        query13.select(courseByTeacher).where(cb3.equal(teacherToJoin.get("lastname"), lastnameParameter));
        List<Course> coursesLastname = em.createQuery(query13).setParameter("lastname", "Ανδρούτσος").getResultList();

        // Teachers with more than one courses
        CriteriaBuilder cb4 = em.getCriteriaBuilder();
        CriteriaQuery<Teacher> query14 = cb4.createQuery(Teacher.class);
        Root<Teacher> teacherRoot = query14.from(Teacher.class);
        query14.select(teacherRoot).where(cb4.greaterThan(cb4.size(teacherRoot.get("courses")), 1));
        List<Teacher> teachersList = em.createQuery(query14).getResultList();

        // get a list with courses title with teachers lastname firstname
        CriteriaBuilder cb5 = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query15 = cb5.createQuery(Object[].class);
        Root<Course> courseRoot = query15.from(Course.class);
        Join<Course, Teacher> teacherToJoin2 = courseRoot.join("teacher");
        query15.multiselect(courseRoot.get("title"), teacherToJoin2.get("lastname"), teacherToJoin2.get("firstname"));
        List<Object[]> coursesTeachers = em.createQuery(query15).getResultList();

        // List all teachers who do not teach any course
        CriteriaBuilder cb6 = em.getCriteriaBuilder();
        CriteriaQuery<Teacher> query16 = cb6.createQuery(Teacher.class);
        Root<Teacher> teacherRoot2 = query16.from(Teacher.class);
        query16.select(teacherRoot2).where(cb6.isEmpty(teacherRoot2.get("courses")));
        List<Teacher> teachersNoCourses = em.createQuery(query16).getResultList();

        // List Teachers with course count
        CriteriaBuilder cb7 = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query17 = cb7.createQuery(Object[].class);
        Root<Teacher> teacherRoot3 = query17.from(Teacher.class);
        Join<Teacher, Course> courseToJoin = teacherRoot3.join("courses", JoinType.LEFT);   // JoinType.LEFT -> returns and the nulls
        query17.multiselect(teacherRoot3, cb7.count(courseToJoin)).groupBy(teacherRoot3);
        List<Object[]> teachersWithCoursesCount = em.createQuery(query17).getResultList();

        // Find teachers who teach SQL
        CriteriaBuilder cb8 = em.getCriteriaBuilder();
        CriteriaQuery<Teacher> query18 = cb8.createQuery(Teacher.class);
        Root<Teacher> teacherRoot4 = query18.from(Teacher.class);
        ParameterExpression<String> value = cb8.parameter(String.class, "title");
            // query18.select(teacherRoot4).where(cb8.equal(teacherRoot4.get("courses").get("title"), "SQL")); // SQL Injection
        query18.select(teacherRoot4).where(cb8.equal(teacherRoot4.get("courses").get("title"), value));
        List<Teacher> teachersWithSQL = em.createQuery(query18).setParameter("title", "SQL").getResultList();

        // Find Teachers with lastname starts with 'A' and teach more than one courses
        CriteriaBuilder cb9 = em.getCriteriaBuilder();
        CriteriaQuery<Teacher> query19 = cb9.createQuery(Teacher.class);
        Root<Teacher> teacherRoot5 = query19.from(Teacher.class);
        ParameterExpression<String> val = cb9.parameter(String.class, "lastname");
        query19.select(teacherRoot5).where(cb9.like(teacherRoot5.get("lastname"), val), cb9.gt(cb9.size(teacherRoot5.get("courses")), 1));
        List<Teacher> teachersLastnameCourseMoreThanOne = em.createQuery(query19).setParameter("lastname", "Α%").getResultList();

        em.getTransaction().commit();

        // Print all teachers.
        teachers.forEach(System.out::println);

        // Print all courses.
        courses.forEach(System.out::println);

        // Print courses taught by Αθανάσιος
        courses3.forEach(System.out::println);

        // Print teachers & course.titles
        for (Object[] row : teachersCourseTitles) {
            for (Object item : row) {
                System.out.print(item + " ");
            }
            System.out.println();
        }

        // Print teachers that teach Java
        teachersJava.forEach(System.out::println);

        // Print teacher.firstname, lastname, count of courses they teach
        for (Object[] row : teachersCoursesCount) {
            for (Object item : row) {
                System.out.print(item + " ");
            }
            System.out.println();
        }

        // Print teachers that teach more than one courses
        teachersMoreThanOneCourses.forEach(System.out::println);

        // Print teachers & courses they teach ordered by lastname, title
        for (Object[] row : teachersCoursesOrdered) {
            for (Object item : row) {
                System.out.print(item + " ");
            }
            System.out.println();
        }

        // Print teachers that do not teach a course
        teachersWithNoCourses.forEach(System.out::println);

        // Print the most popular courses by teachers' count
        for (Object[] row : popularCourses) {
            for (Object item : row) {
                System.out.print(item + " ");
            }
            System.out.println();
        }

        // Print list courses
        titles.forEach(System.out::println);

        // Print teachers with a specific lastname
        teachersLastname.forEach(System.out::println);

        // Print the found courses taught by a specific teacher
        coursesLastname.forEach(System.out::println);

        // Print teachers with more than one courses
        teachersList.forEach(System.out::println);

        // Print a list with courses title with teachers lastname firstname
        for (Object[] row : coursesTeachers) {
            for (Object item : row) {
                System.out.print(item + " ");
            }
            System.out.println();
        }

        // Print a list with all teachers who do not teach any course
        teachersNoCourses.forEach(System.out::println);

        //  Print a list of teachers with course count
        for (Object[] row : teachersWithCoursesCount) {
            for (Object item : row) {
                System.out.print(item + " ");
            }
            System.out.println();
        }

        // Print teachers who teach SQL
        teachersWithSQL.forEach(System.out::println);

        // Print Teachers with lastname starts with 'A' and teach more than one courses
        teachersLastnameCourseMoreThanOne.forEach(System.out::println);

        em.close();
        emf.close();

    }
}
