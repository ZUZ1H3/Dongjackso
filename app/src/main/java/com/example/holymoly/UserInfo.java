package com.example.holymoly;

// firebase DB에 접속한 User 정보
public class UserInfo {
    public String email;
    public String password;
    public String name;
    public Integer age;
    public String gender;

    public UserInfo() {
        // 기본 생성자 필요
    }
    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }

    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

}
