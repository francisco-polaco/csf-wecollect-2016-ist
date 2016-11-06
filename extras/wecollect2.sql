-- phpMyAdmin SQL Dump
-- version 4.6.4
-- https://www.phpmyadmin.net/
--
-- Host: localhost
-- Generation Time: Nov 04, 2016 at 12:46 PM
-- Server version: 5.7.15-log
-- PHP Version: 7.0.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `wecollect`
--

-- --------------------------------------------------------

-- LIMPEZA
SET foreign_key_checks = 0;

DROP TABLE `wecollect`.`computers`, `wecollect`.`fwlogs`, `wecollect`.`logoffs`, `wecollect`.`logons`, `wecollect`.`pwchanges`, `wecollect`.`shutdowns`, `wecollect`.`startups`, `wecollect`.`users`;

SET foreign_key_checks = 1;

--
-- Table structure for table `computers`
--

CREATE TABLE `computers` (
  `id` int(11) UNSIGNED NOT NULL,
  `name` varchar(255) NOT NULL,
  `sid` varchar(40) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `fwlogs`
--

CREATE TABLE `fwlogs` (
  `id` int(11) UNSIGNED NOT NULL,
  `computer_id` int(11) UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL,
  `allowed` tinyint(1) NOT NULL DEFAULT '1',
  `protocol` varchar(10) NOT NULL,
  `src_ip` varbinary(16) NOT NULL,
  `src_port` smallint(5) UNSIGNED NOT NULL,
  `dst_ip` varbinary(16) NOT NULL,
  `dst_port` smallint(5) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `logoffs`
--

CREATE TABLE `logoffs` (
  `id` int(11) UNSIGNED NOT NULL,
  `user_id` int(11) UNSIGNED NOT NULL,
  `logon_id` bigint(20) NOT NULL,
  `login_type` tinyint(4) UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `logons`
--

CREATE TABLE `logons` (
  `id` int(11) UNSIGNED NOT NULL,
  `user_id` int(11) UNSIGNED NOT NULL,
  `logon_id` bigint(20) NOT NULL,
  `login_type` tinyint(4) UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `pwchanges`
--

CREATE TABLE `pwchanges` (
  `id` int(11) UNSIGNED NOT NULL,
  `user_id` int(11) UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL,
  `changed_by` int(11) UNSIGNED NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `shutdowns`
--

CREATE TABLE `shutdowns` (
  `id` int(11) UNSIGNED NOT NULL,
  `computer_id` int(11) UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `startups`
--

CREATE TABLE `startups` (
  `id` int(11) UNSIGNED NOT NULL,
  `computer_id` int(11) UNSIGNED NOT NULL,
  `timestamp` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int(11) UNSIGNED NOT NULL,
  `computer_id` int(11) UNSIGNED NOT NULL,
  `relative_id` varchar(4) NOT NULL,
  `username` varchar(255) NOT NULL,
  `created_by` int(11) UNSIGNED DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `computers`
--
ALTER TABLE `computers`
  ADD PRIMARY KEY (`id`),
  ADD KEY `id` (`id`);

--
-- Indexes for table `fwlogs`
--
ALTER TABLE `fwlogs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `computer_id` (`computer_id`);

--
-- Indexes for table `logoffs`
--
ALTER TABLE `logoffs`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `logons`
--
ALTER TABLE `logons`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `pwchanges`
--
ALTER TABLE `pwchanges`
  ADD PRIMARY KEY (`id`),
  ADD KEY `changed_by` (`changed_by`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `shutdowns`
--
ALTER TABLE `shutdowns`
  ADD PRIMARY KEY (`id`),
  ADD KEY `computer_id` (`computer_id`);

--
-- Indexes for table `startups`
--
ALTER TABLE `startups`
  ADD PRIMARY KEY (`id`),
  ADD KEY `computer_id` (`computer_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD KEY `computer_id` (`computer_id`),
  ADD KEY `created_by` (`created_by`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `computers`
--
ALTER TABLE `computers`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `fwlogs`
--
ALTER TABLE `fwlogs`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `logoffs`
--
ALTER TABLE `logoffs`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `logons`
--
ALTER TABLE `logons`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `pwchanges`
--
ALTER TABLE `pwchanges`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `shutdowns`
--
ALTER TABLE `shutdowns`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `startups`
--
ALTER TABLE `startups`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) UNSIGNED NOT NULL AUTO_INCREMENT;
--
-- Constraints for dumped tables
--

--
-- Constraints for table `fwlogs`
--
ALTER TABLE `fwlogs`
  ADD CONSTRAINT `fwlogs_ibfk_1` FOREIGN KEY (`computer_id`) REFERENCES `computers` (`id`);

--
-- Constraints for table `logoffs`
--
ALTER TABLE `logoffs`
  ADD CONSTRAINT `logoffs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `logons`
--
ALTER TABLE `logons`
  ADD CONSTRAINT `logons_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `pwchanges`
--
ALTER TABLE `pwchanges`
  ADD CONSTRAINT `pwchanges_ibfk_2` FOREIGN KEY (`changed_by`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `pwchanges_ibfk_3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `shutdowns`
--
ALTER TABLE `shutdowns`
  ADD CONSTRAINT `shutdowns_ibfk_1` FOREIGN KEY (`computer_id`) REFERENCES `computers` (`id`);

--
-- Constraints for table `startups`
--
ALTER TABLE `startups`
  ADD CONSTRAINT `startups_ibfk_1` FOREIGN KEY (`computer_id`) REFERENCES `computers` (`id`);

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`computer_id`) REFERENCES `computers` (`id`),
  ADD CONSTRAINT `users_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
