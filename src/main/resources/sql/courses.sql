USE hiber6prod1db;

INSERT INTO `courses` (`id`,`title`,`teacher_id`) VALUES (1,'Java',1);
INSERT INTO `courses` (`id`,`title`,`teacher_id`) VALUES (2,'Cobol',1);
INSERT INTO `courses` (`id`,`title`,`teacher_id`) VALUES (3,'C#',2);
INSERT INTO `courses` (`id`,`title`,`teacher_id`) VALUES (4,'Python',3);
INSERT INTO `courses` (`id`,`title`,`teacher_id`) VALUES (5,'SQL',4);
INSERT INTO `courses` (`id`,`title`,`teacher_id`) VALUES (6,'React',4);

ALTER TABLE courses AUTO_INCREMENT = 7;