-- Audit and Soft Delete Migration Script for IQE
-- This script adds audit UDT and is_active columns to tables that don't have them

USE iqe;

-- Add audit and soft delete to actions table
ALTER TABLE iqe.actions ADD audit frozen<audit>;
ALTER TABLE iqe.actions ADD is_active boolean;

-- Add audit and soft delete to questions table  
ALTER TABLE iqe.questions ADD audit frozen<audit>;
ALTER TABLE iqe.questions ADD is_active boolean;

-- Add audit and soft delete to answer_options table
ALTER TABLE iqe.answer_options ADD audit frozen<audit>;
ALTER TABLE iqe.answer_options ADD is_active boolean;

-- Add audit and soft delete to questions_details table
ALTER TABLE iqe.questions_details ADD audit frozen<audit>;
ALTER TABLE iqe.questions_details ADD is_active boolean;

-- Update existing records to set is_active = true for backward compatibility
UPDATE iqe.actions SET is_active = true WHERE action_id IN (SELECT action_id FROM iqe.actions);
UPDATE iqe.questions SET is_active = true WHERE action_id IN (SELECT action_id FROM iqe.questions);
UPDATE iqe.answer_options SET is_active = true WHERE action_id IN (SELECT action_id FROM iqe.answer_options);
UPDATE iqe.questions_details SET is_active = true WHERE action_id IN (SELECT action_id FROM iqe.questions_details);
