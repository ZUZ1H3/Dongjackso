package com.example.holymoly;

// firebase DB에 등록된 User 정보
public class UserInfo {
    public String email;    // 이메일
    public String password; // 비밀번호
    public String name;     // 이름
    public String nickname; // 업적 이름
    public Integer age;     // 나이
    public String gender;   // 성별
    // public String

    public UserInfo() {
        // 기본 생성자 필요
    }
    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getNickname() { return nickname; }

    public void setNickName(String nickname) { this.nickname = nickname; }

    public int getAge() { return age; }

    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }

    public void setGender(String gender) { this.gender = gender; }

}
