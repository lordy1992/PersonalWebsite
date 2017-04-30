import bcrypt
import getpass

if __name__ == "__main__":
    pw = str.encode(getpass.getpass())
    print(bcrypt.hashpw(pw, bcrypt.gensalt(prefix=b"2a")))
