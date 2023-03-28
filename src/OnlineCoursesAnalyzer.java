import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17
 * (some statements may be not allowed in lower version).
 * This is just a demo, and you can extend and implement functions
 * based on this demo, or implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

  List<Course> courses = new ArrayList<>();

  public OnlineCoursesAnalyzer(String datasetPath) {
    BufferedReader br = null;
    String line;
    try {
      br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
      br.readLine();
      while ((line = br.readLine()) != null) {
        String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
        Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
            Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
            Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
            Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
            Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
            Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
            Double.parseDouble(info[21]), Double.parseDouble(info[22]));
        courses.add(course);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
  }

  //1
  public Map<String, Integer> getPtcpCountByInst() {
    Map<String, Integer> map = courses.stream()./*sorted(Comparator.comparing(Course::getInstitution))*/collect(Collectors.groupingBy(Course::getInstitution, Collectors.summingInt(Course::getParticipants)));
    List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
    list.sort(Comparator.comparing(Map.Entry::getKey));
    Map<String, Integer> ans = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> entry : list) {
      ans.put(entry.getKey(), entry.getValue());
    }
    return ans;
//        return map;
  }

  //2
  public Map<String, Integer> getPtcpCountByInstAndSubject() {
    Map<String, Integer> map = courses.stream().collect(Collectors.groupingBy(Course::getInstAndSubj, Collectors.summingInt(Course::getParticipants)));
    List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
    list.sort(Comparator.comparingInt(Map.Entry<String, Integer>::getValue).reversed().thenComparing(Map.Entry<String, Integer>::getKey));
    Map<String, Integer> ans = new LinkedHashMap<>();
    for (Map.Entry<String, Integer> entry : list) {
      ans.put(entry.getKey(), entry.getValue());
    }
    return ans;
  }

  //3
  public Map<String, List<List<String>>> getCourseListOfInstructor() {
    Map<String, List<List<String>>> map = new HashMap<>();
    courses.forEach(course -> {
      course.instructorList.forEach(itr -> {
        if (!map.containsKey(itr)) {
          List<List<String>> tmp = new ArrayList<>();
          List<String> indiList = new ArrayList<>();
          List<String> codevList = new ArrayList<>();
          tmp.add(indiList);
          tmp.add(codevList);
          map.put(itr, tmp);
        }
        if (course.isIndividual()) {
          if (!map.get(itr).get(0).contains(course.title)) {
            map.get(itr).get(0).add(course.title);
          }
        } else {
          if (!map.get(itr).get(1).contains(course.title)) {
            map.get(itr).get(1).add(course.title);
          }
        }
      });
    });
    map.forEach((key, val) -> {
      val.get(0).sort(Comparator.naturalOrder());
      val.get(1).sort(Comparator.naturalOrder());
    });
//        map.forEach((k,v)-> {
//            System.out.printf("%s == [%s, %s]\n",k,Arrays.toString (v.get(0).toArray()),Arrays.toString (v.get(1).toArray()));
//        });
    return map;
  }

  //4
  public List<String> getCourses(int topK, String by) {
    Comparator<Course> comparator = null;
    if (by.equals("hours")) {
      comparator = Comparator.comparing(Course::getTotalHours)
          .reversed()
          .thenComparing(Course::getTitle);
    } else {
      comparator = Comparator.comparing(Course::getParticipants)
          .reversed()
          .thenComparing(Course::getTitle);
    }
    return courses.stream().sorted(comparator)
        .distinct().limit(topK)
        .map(course -> course.title).toList();
  }

  //5
  public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
    return courses.stream()
        .filter(course -> {
          return course.subject.toUpperCase().contains(courseSubject.toUpperCase());
        })
        .filter(course -> {
          return course.getPercentAudited() >= percentAudited;
        })
        .filter(course -> {
          return course.getTotalHours() <= totalCourseHours;
        })
        .map(course -> course.title)
        .distinct()
        .sorted(Comparator.naturalOrder())
        .toList();
  }

  //6
  public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
    Map<String, Double> avgMedian = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getMedianAge)));
    Map<String, Double> avgMalePct = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentMale)));
    Map<String, Double> avgBachelor = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.averagingDouble(Course::getPercentDegree)));
    Map<String, String> numToTitle = courses.stream().collect(Collectors.groupingBy(Course::getNumber, Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(Course::getLaunchDate)), a -> a.map(Course::getTitle).orElse(""))));
    List<keyInfo> hehe = new ArrayList<>();
    avgMedian.forEach((k, v) -> {
      keyInfo tmp = new keyInfo(k, numToTitle.get(k), v, avgMalePct.get(k), avgBachelor.get(k));
      tmp.setSim(age, gender, isBachelorOrHigher);
      hehe.add(tmp);
    });
    return hehe.stream().sorted(Comparator.comparingDouble(keyInfo::getSim)
            .thenComparing(keyInfo::getTitle))
        .distinct()
        .limit(10)
        .map(a -> a.title)
        .toList();
  }

}

class Course {
  public String getInstitution() {
    return institution;
  }

  String institution;
  String number;
  Date launchDate;

  public String getTitle() {
    return title;
  }

  String title;
  String instructors;
  List<String> instructorList = null;
  String subject;
  int year;
  int honorCode;

  public int getParticipants() {
    return participants;
  }

  int participants;
  int audited;
  int certified;

  public double getPercentAudited() {
    return percentAudited;
  }

  public String getNumber() {
    return number;
  }

  public Date getLaunchDate() {
    return launchDate;
  }

  double percentAudited;
  double percentCertified;
  double percentCertified50;
  double percentVideo;
  double percentForum;
  double gradeHigherZero;

  public String getSubject() {
    return subject;
  }

  public double getTotalHours() {
    return totalHours;
  }

  double totalHours;
  double medianHoursCertification;

  public double getMedianAge() {
    return medianAge;
  }

  double medianAge;

  public double getPercentMale() {
    return percentMale;
  }

  double percentMale;
  double percentFemale;

  public double getPercentDegree() {
    return percentDegree;
  }

  double percentDegree;

  public Course(String institution, String number, Date launchDate,
                String title, String instructors, String subject,
                int year, int honorCode, int participants,
                int audited, int certified, double percentAudited,
                double percentCertified, double percentCertified50,
                double percentVideo, double percentForum, double gradeHigherZero,
                double totalHours, double medianHoursCertification,
                double medianAge, double percentMale, double percentFemale,
                double percentDegree) {
    this.institution = institution;
    this.number = number;
    this.launchDate = launchDate;
    if (title.startsWith("\"")) title = title.substring(1);
    if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
    this.title = title;
    if (instructors.startsWith("\"")) instructors = instructors.substring(1);
    if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
    this.instructors = instructors;
    // get the instructors
    instructorList = new ArrayList<>();
    instructorList.addAll(Arrays.asList(instructors.split(", ")));
    if (subject.startsWith("\"")) subject = subject.substring(1);
    if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
    this.subject = subject;
    this.year = year;
    this.honorCode = honorCode;
    this.participants = participants;
    this.audited = audited;
    this.certified = certified;
    this.percentAudited = percentAudited;
    this.percentCertified = percentCertified;
    this.percentCertified50 = percentCertified50;
    this.percentVideo = percentVideo;
    this.percentForum = percentForum;
    this.gradeHigherZero = gradeHigherZero;
    this.totalHours = totalHours;
    this.medianHoursCertification = medianHoursCertification;
    this.medianAge = medianAge;
    this.percentMale = percentMale;
    this.percentFemale = percentFemale;
    this.percentDegree = percentDegree;
  }

  public boolean isIndividual() {
    return instructorList.size() == 1;
  }

  public String getInstAndSubj() {
    return institution + "-" + subject;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Course)) {
      return false;
    }
    return ((Course) o).title.equals(title);
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }
}

class keyInfo {
  String number;
  String title;
  double avgAge;
  double avgMale;
  double bachelorHigher;

  double sim;

  public keyInfo(String number, String title, double avgAge, double avgMale, double bachelorHigher) {
    this.number = number;
    this.title = title;
    this.avgAge = avgAge;
    this.avgMale = avgMale;
    this.bachelorHigher = bachelorHigher;
  }

  public String getNumber() {
    return number;
  }

  public String getTitle() {
    return title;
  }

  public double getAvgMale() {
    return avgMale;
  }

  public double getBachelorHigher() {
    return bachelorHigher;
  }

  public double getAvgAge() {
    return avgAge;
  }

  public void setSim(int age, int gender, int isBachelorOrHigher) {
    sim = Math.pow(age - avgAge, 2) + Math.pow((100 * gender) - avgMale, 2) + Math.pow((isBachelorOrHigher * 100) - bachelorHigher, 2);
  }

  public double getSim() {
    return sim;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof keyInfo)) {
      return false;
    }
    return ((keyInfo) o).title.equals(title);
  }

  @Override
  public int hashCode() {
    return title.hashCode();
  }
}