-- 用户表
CREATE TABLE IF NOT EXISTS users
(
    user_id  INT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

-- 用户信息表
CREATE TABLE IF NOT EXISTS user_info
(
    user_id   INT PRIMARY KEY,
    full_name VARCHAR(255),
    email     VARCHAR(255),
    CONSTRAINT fk_user_info FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- 角色表
CREATE TABLE IF NOT EXISTS roles
(
    role_id   INT PRIMARY KEY,
    role_name VARCHAR(255) NOT NULL
);

-- 角色信息表
CREATE TABLE IF NOT EXISTS role_info
(
    role_id     INT PRIMARY KEY,
    description TEXT,
    CONSTRAINT fk_role_info FOREIGN KEY (role_id) REFERENCES roles (role_id)
);

-- 菜单表
CREATE TABLE IF NOT EXISTS menus
(
    menu_id   INT PRIMARY KEY,
    menu_name VARCHAR(255) NOT NULL
);

-- 菜单信息表
CREATE TABLE IF NOT EXISTS menu_info
(
    menu_id     INT PRIMARY KEY,
    description TEXT,
    CONSTRAINT fk_menu_info FOREIGN KEY (menu_id) REFERENCES menus (menu_id)
);

-- 功能项表
CREATE TABLE IF NOT EXISTS features
(
    feature_id   INT PRIMARY KEY,
    feature_name VARCHAR(255) NOT NULL
);

-- 功能项信息表
CREATE TABLE IF NOT EXISTS feature_info
(
    feature_id  INT PRIMARY KEY,
    description TEXT,
    CONSTRAINT fk_feature_info FOREIGN KEY (feature_id) REFERENCES features (feature_id)
);

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles
(
    user_id INT,
    role_id INT,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (role_id)
);

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS role_menus
(
    role_id INT,
    menu_id INT,
    PRIMARY KEY (role_id, menu_id),
    CONSTRAINT fk_role_menus_role FOREIGN KEY (role_id) REFERENCES roles (role_id),
    CONSTRAINT fk_role_menus_menu FOREIGN KEY (menu_id) REFERENCES menus (menu_id)
);

-- 菜单功能项关联表
CREATE TABLE IF NOT EXISTS menu_features
(
    menu_id    INT,
    feature_id INT,
    PRIMARY KEY (menu_id, feature_id),
    CONSTRAINT fk_menu_features_menu FOREIGN KEY (menu_id) REFERENCES menus (menu_id),
    CONSTRAINT fk_menu_features_feature FOREIGN KEY (feature_id) REFERENCES features (feature_id)
);
