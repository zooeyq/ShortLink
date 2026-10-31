-- MySQL 8.0.16 或以上版本。
-- 执行前请先选择目标数据库。
CREATE TABLE IF NOT EXISTS short_link (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    short_code VARCHAR(16)
        CHARACTER SET ascii COLLATE ascii_bin
        NOT NULL COMMENT '短码，区分大小写',
    original_url TEXT NOT NULL COMMENT '原始网址',
    enabled TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：0禁用，1启用',
    expires_at DATETIME DEFAULT NULL COMMENT '过期时间，NULL表示长期有效',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',

    PRIMARY KEY (id),
    UNIQUE KEY uk_short_code (short_code),
    CONSTRAINT chk_short_link_enabled CHECK (enabled IN (0, 1))
) ENGINE = InnoDB
  DEFAULT CHARACTER SET = utf8mb4
  COMMENT = '短链接表';
