<?xml version="1.0" encoding="ISO-8859-1"?>
<faqs title="Frequently Asked Questions">
  <part id="general">
    <title>General</title>
    <faq id="profileVariables">
      <question>I use variables in dependencies that are influenced by profiles. This is not working in flattened POM. What can I do?</question>
      <answer>
        <p>In general dynamic dependencies should be avoided. However, you can move the dependency from the direct project dependencies into the dependencies of all relevant profiles.</p>
      </answer>
    </faq>
  </part>
</faqs>
